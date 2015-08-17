package br.uff.testeassinador.controller;

import android.app.Activity;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.util.Log;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;

/**
 * Created by matheus on 03/08/15.
 */
public class CertificateController {

    public void obterCertificados() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, CertStoreException {

        Log.i("Certificates", "Entrou no método obterCertificados ");
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidCAStore");
            Log.i("Certificates", "Obteve KeyStore");
            ks.load(null);
            Log.i("Certificates", "Carregou keystore");
            Enumeration<String> aliases = ks.aliases();
            Log.i("Certificates", String.valueOf(aliases.hasMoreElements()));
            while (aliases.hasMoreElements()) {
                //Log.i("Certificates", aliases.nextElement());
                String alias = aliases.nextElement();
                if (alias.contains("user")) {
                    X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
                    Log.i("Certificates", cert.getSubjectDN().getName());
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assinarDocumento(final Activity activity) {

        KeyChain.choosePrivateKeyAlias(activity,

                new KeyChainAliasCallback() {

                    public void alias(String alias) {
                        // Credential alias selected.  Remember the alias selection for future use.
                        if (alias != null){
                            Object[] params = new Object[2];
                            params[0] = activity;
                            params[1] = alias;

                            AssinarDocumentoTask assinarDocumentoTask = new AssinarDocumentoTask();
                            assinarDocumentoTask.execute(params);
                            try {
                                Boolean retorno = assinarDocumentoTask.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                },
                new String[]{"RSA", "DSA"}, // List of acceptable key types. null for any
                null,                       // issuer, null for any
                null,                       // host name of server requesting the cert, null if unavailable
                -1,                         // port of server requesting the cert, -1 if unavailable
                null);
    }
}
