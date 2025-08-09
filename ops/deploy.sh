# Create resource group and create resources for Azure Functions
cd functions &&
mvn clean package &&
mvn azure-functions:deploy

# Assign an identity to azure functions
# TODO: change name and resource group (conflict)
az functionapp identity assign \
  --name excalifun-java-serverless \ 
  --resource-group excalifun-serverless-functions \
  --debug

# Retrieve the principalId of the function app
principalIdFunctionAppService=$(az functionapp identity show \
  --name excalifun-java-serverless \
  --resource-group excalifun-serverless-functions \
  --query identity.principalId \
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
  --scope $(az keyvault show --name keyvaultexcalifan --query id -o tsv) \
  --debug










# Create resource group for storage
az group create \
    --name ExcalifunStorageGroup \
    --location westeurope \
    --debug

# Create storage account
# Create a container for the storage
az deployment group create \
    --resource-group ExcalifunStorageGroup \
    --template-file arm_blob_storage.json \
    --parameters arm_blob_storage_parameters.json \
    --debug





# TESTED HERE (START)
# Create resource group for SignalR
az group create \
    --name ExcalifunSignalRGroup \
    --location westeurope \
    --debug

# Create the resource for SignalR
az deployment group create \
    --resource-group ExcalifunSignalRGroup \
    --template-file arm_signalr.json \
    --parameters arm_signalr_parameters.json \
    --debug




# Create resource group for keyvault
az group create \
    --name ExcalifunKeyVaultGroup \
    --location westeurope \
    --debug

# Wait until the SignalR resource is created, and so the primary key is available
az signalr wait \
  --name excalifansignalr \
  --resource-group ExcalifunSignalRGroup \
  --created \
  --debug

# Retrieve secret from SignalR
signalRsecret=$(az signalr key list \
  --name excalifansignalr \
  --resource-group ExcalifunSignalRGroup \
  --query primaryKey \
  -o tsv )

echo "SignalR primary key: $signalRsecret"

# Wait until the storage account is created, and so the primary access key is available
az storage account wait \
  --name excalifanstorage \
  --resource-group ExcalifunStorageGroup \
  --created \
  --debug

# Retrieve the primary access key from storage account
blobStoragePrimaryAccessKey=$(az storage account keys list \
  --account-name excalifanstorage \
  --resource-group ExcalifunStorageGroup \
  --query "[0].value" \
  -o tsv)

echo "Storage account primary key: $blobStoragePrimaryAccessKey"

# Create the resources for the deployment
az deployment group create \
  --resource-group ExcalifunKeyVaultGroup \
  --template-file arm_key_vault.json \
  --parameters keyForSignalR_value="$signalRsecret"  \
  --debug





# Create resource group for App Service
# Create Spring server with App Service
cd ../testSignalRInSpring/my-webapp &&
mvn clean package &&
mvn azure-webapp:deploy &&
cd ../../

# Assign the identity to app service
az webapp identity assign \
  --name rest-service-2 \
  --resource-group rest-service-2-rg \
  --debug

# Retrieve the principalId of the deployed app
principalIdAppService=$(az webapp show \
  --name rest-service-2 \
  --resource-group rest-service-2-rg \
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
  --scope $(az keyvault show --name keyvaultexcalifan --query id -o tsv) \
  --debug

# TESTED HERE (END) 