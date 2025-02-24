// Funzione per caricare la pagina di login
function loadLoginPage() {
    const loginHTML = `
        <div class="login-overlay" id="login-overlay">
            <div class="login-form">
                <button class="close-button" onclick="closeLogin()">×</button>
                <img src="logo-exacalifan.png" alt="Logo" class="login-logo">
                
                <!-- 
                <input type="email" placeholder="Email" class="login-input">
                <input type="password" placeholder="Password" class="login-input">
                <button class="login-button">Login</button> 
                -->

                <label class="login-label">Effettua l'accesso con Microsoft per la memorizzazione della board</label>
                <button class="login-button microsoft-login" onclick="loginWithMicrosoft()">
                    <img src="microsoft-logo.png" alt="Microsoft Logo" class="microsoft-logo">
                    Accedi con Microsoft
                </button>
            </div>
        </div>
    `;
    document.getElementById('login-container').innerHTML = loginHTML;
    document.body.classList.add('login-active');
}

// Funzione per gestire l'accesso con Microsoft
function loginWithMicrosoft() {
    // Implementa il flusso di autenticazione di Microsoft qui
    window.location.href = 'https://login.microsoftonline.com/common/oauth2/v2.0/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=https://yourapp.com/auth/callback&response_mode=query&scope=openid%20profile%20email&state=12345';
}

// Funzione per chiudere la scheda di login
function closeLogin() {
    document.getElementById('login-overlay').style.display = 'none';
    document.body.classList.remove('login-active');
}

// Carica la pagina di login all'avvio
loadLoginPage();