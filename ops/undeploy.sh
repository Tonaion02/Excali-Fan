# Delete the storage account
# WARNING: With that we will delete all the resources associated 
# with the storage account
az storage account \ 
    delete --name excalifan-storage-account
     --resource-group excalifan-storage-group

