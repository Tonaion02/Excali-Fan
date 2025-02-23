import * as msal from "@azure/msal-browser";

const msalConfig = {
  auth: {
    clientId: "c751136e-e609-4363-ab87-32206c7674ec",
    authority: "https://login.microsoftonline.com/d5364e8d-9d78-49f0-9636-be777bbe9507",
    redirectUri: "https://rest-service-1735827345127.azurewebsites.net/api/testEntraId",
  },
};

const msalInstance = new msal.PublicClientApplication(msalConfig);

export function login() {
  const loginRequest = {
    scopes: ["openid", "profile", "email"],
  };

  try {
    const loginResponse = msalInstance.loginPopup(loginRequest);
    console.log("Access Token:", loginResponse.accessToken);
  } catch (error) {
    console.error(error);
  }
}
