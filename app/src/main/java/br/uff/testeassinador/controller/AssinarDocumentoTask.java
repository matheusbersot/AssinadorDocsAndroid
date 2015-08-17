package br.uff.testeassinador.controller;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.security.KeyChain;
import android.util.Base64;

import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;

import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;

import br.uff.testeassinador.util.Constantes;

/**
 * Created by matheus on 03/08/15.
 */
public class AssinarDocumentoTask extends AsyncTask<Object, Void, Boolean> {

    static {
        if (Security.getProvider(Constantes.BOUNCY_CASTLE_PROVIDER) == null) {
            Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        }
    }

    private Exception error;
    private final String TAG = AssinarDocumentoTask.class.getSimpleName();

    @Override
    protected Boolean doInBackground(Object... params) {

        try {

            Context ctx = (Activity) params[0];
            String alias = (String)  params[1];

            //recuperando chave privada e cadeia de certificados
            PrivateKey pk = KeyChain.getPrivateKey(ctx, alias);
            X509Certificate[] chain = KeyChain.getCertificateChain(ctx, alias);

            //certificado usado para codificar dados usando a chave privada
            X509CertificateHolder x509CertHolder = new X509CertificateHolder(chain[0].getEncoded());

            // dados que serão assinados
            StorageController storageController = new StorageController();

            //byte[] data = "foobar".getBytes("ASCII");
            byte[] data = storageController.getFileData("teste.pdf");
            CMSTypedData inf = new CMSProcessableByteArray(data);

            // algoritmo usado para assinar dados
            // foi usado o JCA provider default, visto que não descobri um JCA provider no Android de onde saiu a PrivateKey acima
            ContentSigner sha1Signer = new JcaContentSignerBuilder(Constantes.ALG_SHA1_WITH_RSA).build(pk);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            gen.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().setProvider(Constantes.BOUNCY_CASTLE_PROVIDER).build())
                            .build(sha1Signer, x509CertHolder));

            gen.addCertificate(x509CertHolder);

            //criando uma assinatura pkcs7 detached
            CMSSignedData signedData = gen.generate(inf, false);
            String signatureB64 = Base64.encodeToString(signedData.getEncoded(), 0);

            System.out.println(signatureB64);

            //verificando assinatura
            boolean valid = false;

            Store certStore = signedData.getCertificates();
            SignerInformationStore signerStore = signedData.getSignerInfos();
            Collection c = signerStore.getSigners();

            Iterator it = c.iterator();
            while (it.hasNext()) {

                SignerInformation signer = (SignerInformation) it.next();
                Collection certCollection = certStore.getMatches(signer.getSID());

                Iterator certIt = certCollection.iterator();
                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();

                X509Certificate certFromSignedData = new JcaX509CertificateConverter().
                                                        setProvider(Constantes.BOUNCY_CASTLE_PROVIDER).getCertificate(certHolder);

                if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().
                                        setProvider(Constantes.BOUNCY_CASTLE_PROVIDER).build(certFromSignedData))) {
                    valid = true;
                    System.out.println("Signature verified");
                } else {
                    System.out.println("Signature verification failed");
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            error = e;
            return null;
        }


    }
}
