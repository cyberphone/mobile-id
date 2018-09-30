java -cp \github.repositories\saturn\resources\common.lib\bcprov-jdk15on-151.jar;\github.repositories\saturn\resources\common.lib\webpki.org-libext-1.00.jar org.webpki.ca.CommandLineCA -ca/keystore \github.repositories\mobile-id\resources\keys\laposte-sub-ca.p12 -ca/storetype PKCS12 -ca/storepass foo123 -ca/keypass foo123 -subject "cn=Luke Skywalker, serialNumber=345606782954" -validity/start "2018-09-25T00:00:00" -validity/end "2020-09-24T23:59:59" -out/keystore "%~dp0lukeskywalker.p12" -out/storetype pkcs12 -out/keypass foo123 -out/storepass foo123 -sigalg ECDSA_SHA256 -ecccurve NIST_P_256 -extension/ocsp http://ocsp.laposte.fr/status -entity/ee -extension/ku digitalSignature
keytool -exportcert -keystore "%~dp0lukeskywalker.p12" -alias mykey -storepass foo123 -storetype PKCS12 -file "%~dp0lukeskywalker.cer"