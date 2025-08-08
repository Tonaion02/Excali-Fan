# # Create resource group for storage
# az group create \
#     --name ExcaliFunStorage \
#     --location westeurope

# # Create storage account
# az storage account create \
#     --name ExcalifanStorageAccount \
#     --resource-group ExcaliFunStorage \
#     --location westeurope \
#     --sku Standard_LRS \
#     --kind StorageV2 \
#     --min-tls-version TLS1_2 \
#     --allow-blob-public-access false

# # Create a container for the storage
# az deployment group create \
#   --resource-group ExcaliFunStorage \
#   --template-file config_blob_storage.json \
#   --parameters storageAccountName=ExcalifanStorageAccount


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
