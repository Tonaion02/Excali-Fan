# Parameters to pass to the script (START)
resource_signalr="excalifansignalr"
resource_group_signalr="ExcalifunSignalRGroup"
resource_storage="excalifanstorage"
resource_group_storage="ExcalifunStorageGroup"
resource_key_vault="keyvaultexcalifan2"
resource_group_key_vault="ExcalifunKeyVaultGroup"
resource_app_service="rest-service-2"
resource_group_app_service="rest-service-2-rg"
resource_function_app_service="excalifun-java-serverless-2"
resource_group_function_app_service="excalifun-serverless-functions-2"
# Parameters to pass to the script (END)

url_app_service="https://$resource_app_service.azurewebsites.net/"
echo "$url_app_service"
url_key_vault="https://$resource_key_vault.vault.azure.net"
echo "$url_key_vault"
url_signalr_service="https://$resource_signalr.service.signalr.net"
echo "$url_signalr_service"



# TESTED HERE (START)
# Create resource group for storage
az group create \
    --name "$resource_group_storage" \
    --location westeurope \
    --debug

# Create storage account
# Create a container for the storage
#     --parameters arm_blob_storage_parameters.json \
az deployment group create \
    --resource-group "$resource_group_storage" \
    --template-file arm_blob_storage.json \
    --parameters storageAccounts_excalifunstorage_name="$resource_storage" \
    --debug



# Create resource group for SignalR
az group create \
    --name "$resource_group_signalr" \
    --location westeurope \
    --debug

# Create the resource for SignalR
#     --parameters arm_signalr_parameters.json \
az deployment group create \
    --resource-group "$resource_group_signalr" \
    --template-file arm_signalr.json \
    --parameters SignalR_SignalRResourceForSpring_name="$resource_signalr" \
    --debug




# Create resource group for keyvault
az group create \
    --name "$resource_group_key_vault" \
    --location westeurope \
    --debug

# Wait until the SignalR resource is created, and so the primary key is available
az signalr wait \
  --name "$resource_signalr" \
  --resource-group "$resource_group_signalr" \
  --created \
  --debug

# Retrieve secret from SignalR
signalRsecret=$(az signalr key list \
  --name "$resource_signalr" \
  --resource-group "$resource_group_signalr" \
  --query primaryKey \
  -o tsv )

echo "SignalR primary key: $signalRsecret"

# Wait until the storage account is created, and so the primary access key is available
az storage account wait \
  --name "$resource_storage" \
  --resource-group "$resource_group_storage" \
  --created \
  --debug

# Retrieve the primary access key from storage account
blobStoragePrimaryAccessKey=$(az storage account keys list \
  --account-name "$resource_storage" \
  --resource-group "$resource_group_storage" \
  --query "[0].value" \
  -o tsv)

echo "Storage account primary key: $blobStoragePrimaryAccessKey"

# Create the resources for the deployment
az deployment group create \
  --resource-group "$resource_group_key_vault" \
  --template-file arm_key_vault.json \
  --parameters keyForSignalR_value="$signalRsecret" keyForStorage_value="$blobStoragePrimaryAccessKey" vaults_testkeyvault10000_name="$resource_key_vault" \
  --debug





# Write in the env file some setup information (START)
touch .env

echo "keyVaultUrl=$url_key_vault" >> .env
echo "signalRServiceBaseEndpoint=$url_signalr_service" >> .env
echo "storageAccountName=$url_key_vault" >> .env

mv .env ../testSignalRInSpring/my-webapp/.env
# Write in the env file some setup information (END)

# Create resource group for App Service
# Create Spring server with App Service
cd ../testSignalRInSpring/my-webapp &&
mvn clean package -DresourceGroup="$resource_group_app_service" -DappName="$resource_app_service" &&
mvn azure-webapp:deploy -DresourceGroup="$resource_group_app_service" -DappName="$resource_app_service" &&
cd ../../

# Assign the identity to app service
az webapp identity assign \
  --name "$resource_app_service" \
  --resource-group "$resource_group_app_service" \
  --debug

# Retrieve the principalId of the deployed app
principalIdAppService=$(az webapp show \
  --name "$resource_app_service" \
  --resource-group "$resource_group_app_service" \
  --query identity.principalId \
  -o tsv)

# Wait for the managed identity to be available
echo "Waiting for managed identity of Web App Service to be available in AAD..."
for i in {1..10}; do
  if az ad sp show --id "$principalIdAppService" &>/dev/null; then
    echo "Managed identity is now available."
    break
  fi
  echo "Still waiting for identity to propagate... ($i/10)"
  sleep 5
done

# Assign privileges to app service to retrieve secrets from KeyVault
az role assignment create \
  --assignee "$principalIdAppService" \
  --role "Key Vault Secrets User" \
  --scope $(az keyvault show --name "$resource_key_vault" --query id -o tsv) \
  --debug

# TESTED HERE (END)





# Embed constants in Constants file (START)
touch Constants.java

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
# Embed constants in Constants file (END)


# Create resource group and create resources for Azure Functions
cd functions &&
mvn clean package -DfunctionAppName="$resource_function_app_service" -DresourceGroupName="$resource_group_function_app_service" &&
mvn azure-functions:deploy -DfunctionAppName="$resource_function_app_service" -DresourceGroupName="$resource_group_function_app_service"

# Assign an identity to azure functions
# TODO: change name and resource group (conflict)
az functionapp identity assign \
  --name "$resource_function_app_service" \
  --resource-group "$resource_group_function_app_service" \
  --debug

# Retrieve the principalId of the function app
principalIdFunctionAppService=$(az functionapp identity show \
  --name "$resource_function_app_service" \
  --resource-group "$resource_group_function_app_service" \
  --query principalId \
  -o tsv)

# Wait for the managed identity of Function App Service to be available
echo "Waiting for managed identity of Function App Service to be available in AAD..."
for i in {1..10}; do
  if az ad sp show --id "$principalIdFunctionAppService" &>/dev/null; then
    echo "Managed identity is now available."
    break
  fi
  echo "Still waiting for identity to propagate... ($i/10)"
  sleep 5
done  

# Assign privileges to function app service to retrieve secrets from KeyVault
az role assignment create \
  --assignee "$principalIdFunctionAppService" \
  --role "Key Vault Secrets User" \
  --scope $(az keyvault show --name "$resource_key_vault" --query id -o tsv) \
  --debug