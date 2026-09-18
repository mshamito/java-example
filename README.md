# Пример использования КриптоПро JCP/JCSP в Spring
В данном примере реализован REST API сервис для создания и проверки CAdES подписи, CMS шифрования, сырой подписи и клиентского TLS, в том числе и двухстороннего

# Предустановка
## java 8 only
* установить JCP/JCSP в java 8
* скачать и скопировать файлы bcpkix-jdk18on-1.78.1.jar, bcprov-jdk18on-1.78.1.jar, bcutil-jdk18on-1.78.1.jar в JRE/lib/ext

## java 11+
* скопировать из дистрибутива AdES-core, ASN1P, asn1rt, CAdES, forms_rt, JCP, JCPRequest, JCPRevCheck, JCPRevTools, JCryptoP, \[JCSP\], \[Rutoken\], \[cpSSL / sspiSSL\] в PROJECT_DIR/libs

# Настройка
* указать считыватель, алиас (имя контейнера) и пин код в application.yml
* установить корневой и промежуточный сертификаты в cacerts

# Сборка
```shell
./gradlew build
```
# Сборка под более старую java, если установлена более новая
```shell
JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 ./gradlew build
```


# Запуск
```shell
/path/to/jre/java -jar build/libs/example.war

```

# Использование
## подпись
```shell
curl http://localhost:8080/sign -F data=@DATA [ -F tsp=http://TSP_SERVER/tsp/tsp.srf -F type=TYPE -F detached=true -F encodeToB64=true ]
```
где  
DATA - файл который необходимо подписать  
TSP_SERVER - адрес сервера службы TSP  
TYPE - тип подписи. допустимые значения: BES / T / XLT1 / A  
detached - отсоединенная (true) или присоединенная (false) подпись  
encodeToB64 - в какой кодировке вернуть подпись. Base64 (true) или DER (false) 

## проверка подписи
```shell
curl http://localhost:8080/verify -F sign=@SIGN [ -F data=@DATA ]
```
где  
SIGN - файл подписи  
DATA - исходный файл для проверки в случае detached подписи  

## зашифрование
```shell
curl http://localhost:8080/encr -F data=@DATA [ -F cert=@CERT1 -F cert=@CERT2 -F encodeToB64=true ]
```
где  
DATA - файл который необходимо зашифровать  
CERTN - сертификат(ы) получателя(ей)   
encodeToB64 - в какой кодировке вернуть подпись. Base64 (true) или DER (false)

## расшифрование
```shell
curl http://localhost:8080/decr -F cms=@CMS 
```
где  
CMS - файл который необходимо расшифровать  

## создание сырой подписи
```shell
curl http://localhost:8080/raw/sign -F data=@DATA [ -F encodeToB64=true -F invert=false]
```
где  
DATA - файл который необходимо подписать  
encodeToB64 - в какой кодировке вернуть подпись. Base64 (true) или DER (false)  
invert - перевернуть ли значение подписи (invert Endianness)  


## проверка сырой подписи
```shell
curl http://localhost:8080/raw/verify -F data=@DATA [ -F cert=@CERT -F signBase64="base64" -F signBinary=@sign.bin -F invert=false]
```
где  
DATA - файл который необходимо зашифровать  
CERT - сертификат для проверки
signBinary - бинарная подпись в файле  
signBase64 - подпись в base64 строке  
invert - перевернуть ли значение подписи (invert Endianness)

## построение цепочки сертификата и проверка на отзыв
```shell
curl http://localhost:8080/cert -F cert=@CERT
```
где  
CERT - сертификат для проверки

## гост tls
```shell
curl localhost:8080/tls -F url="https://cryptopro.ru" [ -F mTLS=false ]
```
где  
url - адрес для подключения  
mTLS - использовать ли 2х сторонний тлс

# Доступен Swagger
```http://localhost:8080```
