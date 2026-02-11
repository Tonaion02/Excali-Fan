# T: In this file is contained the code that is used to generate constants for serverless

source ./parameters.sh

url_app_service="https://$resource_app_service.azurewebsites.net/"
url_app_service_for_cors="https://$resource_app_service.azurewebsites.net"
echo "$url_app_service"
url_key_vault="https://$resource_key_vault.vault.azure.net"
echo "$url_key_vault"
url_signalr_service="https://$resource_signalr.service.signalr.net"
echo "$url_signalr_service"
url_function_app_service="https://$resource_function_app_service.azurewebsites.net"
echo "$url_function_app_service"

# T: Embed constants in Constants file (START)
touch Constants.java

> Constants.java

echo "package com.fabrikam;" >> Constants.java
echo "public class Constants {" >> Constants.java
echo "public static String secretNameBlobStorageAccount = \"keyForBlobStorage\";" >> Constants.java
echo "public static String keyVaultUrl = \""$url_key_vault"\";" >> Constants.java
echo "public static String containerName = \"boardstorage\";" >> Constants.java
echo "public static String accountKeyBlobStorage = \""$blobStoragePrimaryAccessKey"\";" >> Constants.java
echo "public static String storageAccountName = \""$resource_storage"\";" >> Constants.java
echo "public static String appService = \""$url_app_service"\";" >> Constants.java
echo "}" >> Constants.java

mv Constants.java ../functions/src/main/java/com/fabrikam/Constants.java
# T: Embed constants in Constants file (END)