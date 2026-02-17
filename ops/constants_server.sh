# T: In this file is contained the code that is used to generate constants for server

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

# T: Write in the env file some setup information (START)
touch .env

> .env

echo "keyVaultUrl=$url_key_vault" >> .env
echo "signalRServiceBaseEndpoint=$url_signalr_service" >> .env
echo "storageAccountName=$resource_storage" >> .env

mv .env ../webapp/src/main/resources/.env
# T: Write in the env file some setup information (END)

# T: Create constants.js file for the single page application (START)
touch constants.js

> constants.js

echo "var const_appservice = \""$url_app_service"\";" >> constants.js
echo "var const_serverless_service = \""$url_function_app_service"\";" >> constants.js
echo "var const_redirectUri = \""$url_app_service"\";" >> constants.js
echo "var const_clientId = \"b1453203-8719-4a2a-8cc6-96bf883a7e65\";" >> constants.js

mv constants.js ../webapp/src/main/resources/static/constants.js
# T: Create constants.js file for the single page application (ENV)