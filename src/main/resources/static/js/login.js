document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.getElementById('loginForm');
    if (!loginForm) return;
    const submitBtn = loginForm.querySelector('button[type="submit"]');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    loginForm.addEventListener('submit', function (e) {
        if (!usernameInput.value.trim() || !passwordInput.value.trim()) {
            e.preventDefault();
            alert('Please enter both username and password');
            return;
        }
        submitBtn.classList.add('loading');
        submitBtn.disabled = true;
    });

    const errorAlert = document.querySelector('.alert-danger');
    if (errorAlert) {
        const loginCard = document.querySelector('.login-card');
        if (loginCard) {
            loginCard.classList.add('shake');
            setTimeout(() => loginCard.classList.remove('shake'), 500);
        }
        usernameInput.focus();
        usernameInput.select();
    }

    usernameInput && usernameInput.focus();

    document.addEventListener('keydown', function (e) {
        if (e.altKey && e.code === 'KeyL') {
            e.preventDefault();
            usernameInput.focus();
        }
        if (e.code === 'Escape') {
            usernameInput.value = '';
            passwordInput.value = '';
            usernameInput.focus();
        }
    });

    setTimeout(() => {
        submitBtn.classList.remove('loading');
        submitBtn.disabled = false;
    }, 100);

    // Add click functionality to credential cards
    const credentialCards = document.querySelectorAll('.credential-card');
    credentialCards.forEach(card => {
        card.addEventListener('click', function () {
            const username = this.querySelector('strong').nextSibling.textContent.trim();
            const password = this.querySelectorAll('strong')[1].nextSibling.textContent.trim();

            usernameInput.value = username;
            passwordInput.value = password;

            // Add visual feedback
            this.style.background = '#e3f2fd';
            setTimeout(() => {
                this.style.background = '';
            }, 1000);
        });
    });
});


