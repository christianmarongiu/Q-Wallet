# Q Wallet 1.3

Q Wallet è un archivio locale e offline per conservare e visualizzare immagini di QR Code e codici a barre.

## Identità dell'app

- Nome visualizzato: `Q Wallet`
- Nome progetto Gradle: `Q-Wallet`
- Versione: `1.3`
- Nome APK generato: `Q-Wallet_1.3.apk`
- Nome artefatto GitHub Actions: `Q-Wallet_1.3`

## Funzioni principali

- 8 elementi iniziali in una griglia a 2 colonne.
- Pulsante `+` per aggiungere nuovi elementi.
- Label personalizzata, miniatura facoltativa e immagine QR Code o codice a barre.
- Lista dinamica nel formato `N: vuoto` oppure `N: label inserita dall'utente`.
- Spunte verdi per miniatura e codice presenti.
- Funzionamento offline.
- Importazione ed esportazione di label e immagini in un unico file ZIP.

## Compilazione

Il workflow `.github/workflows/build-apk.yml` compila il progetto e pubblica l'artefatto `Q-Wallet_1.3`, contenente il file installabile `Q-Wallet_1.3.apk`.
