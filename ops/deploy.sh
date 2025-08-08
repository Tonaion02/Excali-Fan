





# Create resource group for SignalR
az group create \
    --name ExcalifunSignalRGroup \
    --location westeurope

# Create the resource for SignalR
az deployment group create \
    --resource-group ExcalifunSignalRGroup \
    --template-file arm_signalr.json \
    --parameters arm_signalr_parameters.json





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