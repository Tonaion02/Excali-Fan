const const_redirectUri = "https://rest-service-1735827345127.azurewebsites.net/" 
const const_clientId = "b1453203-8719-4a2a-8cc6-96bf883a7e65";

const msalConfig = {
  auth: {
    clientId: const_clientId, 
    authority: "https://login.microsoftonline.com/common",
    redirectUri: const_redirectUri, // T: TODO:substitute_constant
  },
  cache: {
    cacheLocation: "sessionStorage",
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message, containsPii) => {
        if (containsPii) return;
        switch (level) {
          case msal.LogLevel.Error:
            console.error(message);
            break;
          case msal.LogLevel.Info:
            console.info(message);
            break;
          case msal.LogLevel.Verbose:
            console.debug(message);
            break;
          case msal.LogLevel.Warning:
            console.warn(message);
            break;
        }
      },
    },
  },
};

let msalInstance;

import("https://alcdn.msauth.net/browser/2.38.2/js/msal-browser.min.js").then(() => {
  console.log("MSAL loaded");
  msalInstance = new msal.PublicClientApplication(msalConfig);
});

const loginRequest = {
  scopes: ["openid", "profile", "email", "api://" + const_clientId + "/loginScope"],
  prompt: "select_account",
};

const tokenRequest = {
  scopes: ["openid", "profile", "email", "api://" + const_clientId + "/loginScope"],
  prompt: "select_account",
};





function extractEmailFromToken(accessToken) {
  // T: Decrypt the token and get the payload
  const payloadBase64 = accessToken.split(".")[1];
  const payloadDecoded = JSON.parse(atob(payloadBase64));

  // T: extract email from payload(can be in email or in upn)
  const userEmail = payloadDecoded.email || payloadDecoded.upn;
  console.log("Email of user:", userEmail);

  return userEmail;
}

function retrieveToken() {
  const tokenKey = Object.keys(sessionStorage).find(key => key.includes("accesstoken"));
  const info = sessionStorage.getItem(tokenKey);
  const accessToken = JSON.parse(info).secret;

  console.log("Token found:", accessToken);

  return accessToken;
}



async function login() {
  try {
    if (!msalInstance) {
      console.error("MSAL instance not initialized");
      return;
    }

    console.log("Starting login");
    const loginResponse = await msalInstance.loginPopup(loginRequest);
    console.log("Login successful");

    // Set active account
    msalInstance.setActiveAccount(loginResponse.account);

    let tokenResponse;
    try {
      // Try acquiring token silently
      tokenResponse = await msalInstance.acquireTokenSilent({
        ...tokenRequest,
        account: loginResponse.account,
      });
    } catch (silentError) {
      console.warn("Silent token acquisition failed, acquiring token via popup...", silentError);
      tokenResponse = await msalInstance.acquireTokenPopup(tokenRequest);
    }

    console.log("Access Token:", tokenResponse.accessToken);

    // T: extract email from token
    let email = extractEmailFromToken(tokenResponse.accessToken);


    // T: verify if the token is valid (START)
    // T: TODO remove the email from the request, bad practice for the security
    // T: NOTE: we use mic to say microsoft
    // T: TODO: substitute_constant
    // axios.post("https://rest-service-1735827345127.azurewebsites.net/publicApi/login", {"email" : email}, {
    axios.post(const_redirectUri + "/publicApi/login", {"email" : email}, {
      headers: {
        "Authorization": tokenResponse.accessToken,
        "Content-Type": "application/json"
      }
    })
      .then(response => {
        console.log("Status verification token:", response.status);

        if(response.status == 200) {
          let boardSessionid = response.data;
          console.log("BoardSessionId: " + boardSessionid);
          
          data.groupId = boardSessionid;
          // T: WARNING for now we don't have a different UserId for each user
          // so we simply copy the groupId
          data.userId = email;
          


          const loginContainer = document.getElementById("login-container");
          loginContainer.style.display = "none";
          setup();
        }
      })
      .catch(error => {
        console.error("Error during verification of token:", error);
      });
    // T: verify if the token is valid (END)
  } catch (error) {
    console.error("Login or token retrieval failed:", error);

    showError();
  }
}