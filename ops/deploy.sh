# Create resource group for storage
az group create \
    --name ExcalifunStorageGroup \
    --location westeurope

# Create storage account
# Create a container for the storage
az deployment group create \
    --resource-group ExcalifunStorageGroup \
    --template-file arm_blob_storage.json \
    --parameters arm_blob_storage_parameters.json





# Create resource group for SignalR
az group create \
    --name ExcalifunSignalRGroup \
    --location westeurope

# Create the resource for SignalR
az deployment group create \
    --resource-group ExcalifunSignalRGroup \
    --template-file arm_signalr.json \
    --parameters arm_signalr_parameters.json

# TESTED HERE (START)
# Create resource group for keyvault
az group create \
    --name ExcalifunKeyVaultGroup \
    --location westeurope \
    --debug

# Wait until the SignalR resource is created, and so the primary key is available
az signalr wait \
  --name excalifansignalr \
  --resource-group ExcalifunSignalRGroup \
  --created

# Retrieve secret from SignalR
signalRsecret=$(az signalr key list \
  --name excalifansignalr \
  --resource-group ExcalifunSignalRGroup \
  --query primaryKey \
  -o tsv )

echo "SignalR primary key: $signalRsecret"

# Create the resources for the deployment
az deployment group create \
  --resource-group ExcalifunKeyVaultGroup \
  --template-file arm_key_vault.json \
  --parameters keyForSignalR_value="$signalRsecret" \
  --debug





# Create resource group for App Service
# Create Spring server with App Service
cd ../testSignalRInSpring/my-webapp &&
mvn clean package &&
mvn azure-webapp:deploy

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
echo "Waiting for managed identity to be available in AAD..."
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