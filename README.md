# XML Digital Signature with Smart Card

Sign XML documents using smart card readers, such as the Portuguese Citizen Card.

## Description

This project allows you to digitally sign XML documents using a smart card (e.g., the Portuguese Citizen Card) through a PKCS#11 interface. It utilizes Java and the [Digital Signature Services (DSS)](https://ec.europa.eu/digital-building-blocks/sites/display/DIGITAL/) library to generate XAdES enveloped signatures.

The script was written to run in MacOS, feel free to adapt it to your needs.

## Dependencies

- Java (tested with Java 17)
- Maven
- [OpenSC](https://github.com/OpenSC/OpenSC) (for PKCS#11 smart card access)
- Optional: You can use the library packaged by Autenticação.gov (the pin will be also asked in a GUI dialog)

Make sure your smart card reader is installed and accessible via OpenSC.

## Usage

To build the project:

```bash
mvn package
chmod +x sign.sh
```

To sign documents:

```bash
./sign.sh --pin 1234 example.xml
```

To list the available slots.

```bash
pkcs11-tool --list-slots
```

The pteid provides two certificates - signing and authentication. You will want to choose the signing one:

To list the available keys (example in slot 1):
```bash
pkcs11-tool --list-objects --type cert --slot 1
```


## Options

```bash
Usage: Pkcs11XmlSigner [-hV] [--algorithm=<algorithm>] [--key-index=<keyIndex>]
                       [--level=<signatureLevel>]
                       [--packaging=<signaturePackaging>] --pin=<pin>
                       [--pkcs11-lib=<pkcs11LibPath>] [--slot=<slot>] file
Sign XML files using PKCS11.
      file            The XML file to sign.
      --algorithm=<algorithm>
                      The algorithm to sign. Defaults to SHA256.
  -h, --help          Show this help message and exit.
      --key-index=<keyIndex>
                      The index of the key to be used. Defaults to 0. NOTE: If
                        using the pteid official library use slot 0 and key
                        index 1.
      --level=<signatureLevel>
                      The signature level. Defaults to XAdES_BASELINE_B.
      --packaging=<signaturePackaging>
                      The signature packaging. Defaults to ENVELOPED.
      --pin=<pin>     The pin to access your signing asset. If using the pteid
                        library it will still ask for the pin in a GUI dialog.
      --pkcs11-lib=<pkcs11LibPath>
                      The library used to access the card reader. Defaults to
                        /usr/local/lib/opensc-pkcs11.so
      --slot=<slot>   The slot in the card where the signing token is. Defaults
                        to 1 (with pteid cards)
  -V, --version       Print version information and exit.
```
Ensure your smart card is inserted and the reader is connected before running the application.