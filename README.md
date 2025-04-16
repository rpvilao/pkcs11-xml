# XML Digital Signature with Smart Card

Sign XML documents using smart card readers, such as the Portuguese Citizen Card.

## 📄 Description

This project allows you to digitally sign XML documents using a smart card (e.g., the Portuguese Citizen Card) through a PKCS#11 interface. It utilizes Java and the [Digital Signature Services (DSS)](https://ec.europa.eu/digital-building-blocks/wikis/display/DIGITAL/DS+Library) library to generate XAdES enveloped signatures.

The script was written to run in MacOS, feel free to adapt it to your needs.

## 🧰 Dependencies

- Java (tested with Java 17)
- Maven
- [OpenSC](https://github.com/OpenSC/OpenSC) (for PKCS#11 smart card access)

Make sure your smart card reader is installed and accessible via OpenSC.

## 🚀 Usage

To build the project:

```bash
mvn package
chmod +x sign.sh
```

To sign documents:

```bash
./sign.sh --pin 1234 example.xml
```

Ensure your smart card is inserted and the reader is connected before running the application.