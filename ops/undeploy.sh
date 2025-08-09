# WARNING: Deleting the resource group cause the deletion of
# all the resources that are contained in the resource group.

# Delete the resource group for SignalR
az group delete --name ExcalifunSignalRGroup --yes --no-wait

# Delete the resource group for Blob Storage
az group delete --name ExcalifunStorageGroup --yes --no-wait

# Delete the resource group fro App Service
az group delete --name rest-service-2-rg --yes --no-wait

# Delete the resource group for keyvault
az group delete --name ExcalifunKeyVaultGroup --yes --no-wait