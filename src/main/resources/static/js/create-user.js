document.addEventListener('DOMContentLoaded', function () {
    // Create animated background particles
    function createParticles() {
        const bgAnimation = document.getElementById('bgAnimation');
        const particleCount = 20;

        for (let i = 0; i < particleCount; i++) {
            const particle = document.createElement('div');
            particle.className = 'particle';
            particle.style.width = Math.random() * 10 + 5 + 'px';
            particle.style.height = particle.style.width;
            particle.style.left = Math.random() * 100 + '%';
            particle.style.top = Math.random() * 100 + '%';
            particle.style.animationDelay = Math.random() * 6 + 's';
            particle.style.animationDuration = (Math.random() * 3 + 3) + 's';
            bgAnimation.appendChild(particle);
        }
    }

    createParticles();

    const form = document.getElementById('createUserForm');
    const submitBtn = document.getElementById('submitBtn');

    // Username availability check
    const usernameInput = document.getElementById('username');
    const usernameCheck = document.getElementById('usernameCheck');
    let usernameTimeout;

    usernameInput.addEventListener('input', function () {
        clearTimeout(usernameTimeout);
        const username = this.value.trim();

        if (username.length < 3) {
            usernameCheck.innerHTML = '';
            this.classList.remove('is-valid', 'is-invalid');
            return;
        }

        usernameCheck.innerHTML = '<i class="fas fa-spinner fa-spin checking"></i>';

        usernameTimeout = setTimeout(() => {
            fetch(`/users/check-username?username=${encodeURIComponent(username)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.exists) {
                        usernameCheck.innerHTML = '<i class="fas fa-times unavailable" title="Username unavailable"></i>';
                        usernameInput.classList.add('is-invalid');
                        usernameInput.classList.remove('is-valid');
                    } else {
                        usernameCheck.innerHTML = '<i class="fas fa-check available" title="Username available"></i>';
                        usernameInput.classList.add('is-valid');
                        usernameInput.classList.remove('is-invalid');
                    }
                })
                .catch(() => {
                    usernameCheck.innerHTML = '';
                    usernameInput.classList.remove('is-valid', 'is-invalid');
                });
        }, 500);
    });

    // Email availability check
    const emailInput = document.getElementById('email');
    const emailCheck = document.getElementById('emailCheck');
    let emailTimeout;

    emailInput.addEventListener('input', function () {
        clearTimeout(emailTimeout);
        const email = this.value.trim();

        if (!email || !email.includes('@')) {
            emailCheck.innerHTML = '';
            this.classList.remove('is-valid', 'is-invalid');
            return;
        }

        emailCheck.innerHTML = '<i class="fas fa-spinner fa-spin checking"></i>';

        emailTimeout = setTimeout(() => {
            fetch(`/users/check-email?email=${encodeURIComponent(email)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.exists) {
                        emailCheck.innerHTML = '<i class="fas fa-times unavailable" title="Email unavailable"></i>';
                        emailInput.classList.add('is-invalid');
                        emailInput.classList.remove('is-valid');
                    } else {
                        emailCheck.innerHTML = '<i class="fas fa-check available" title="Email available"></i>';
                        emailInput.classList.add('is-valid');
                        emailInput.classList.remove('is-invalid');
                    }
                })
                .catch(() => {
                    emailCheck.innerHTML = '';
                    emailInput.classList.remove('is-valid', 'is-invalid');
                });
        }, 500);
    });

    // Password strength checker
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const strengthBar = document.getElementById('strengthBar');
    const strengthText = document.getElementById('strengthText');

    function checkPasswordStrength(password) {
        let strength = 0;
        if (password.length >= 8) strength += 1;
        if (/[a-z]/.test(password)) strength += 1;
        if (/[A-Z]/.test(password)) strength += 1;
        if (/[0-9]/.test(password)) strength += 1;
        if (/[^A-Za-z0-9]/.test(password)) strength += 1;
        return strength;
    }

    passwordInput.addEventListener('input', function () {
        const password = this.value;
        const strength = checkPasswordStrength(password);

        strengthBar.className = 'password-strength-bar';
        if (strength >= 4) {
            strengthBar.classList.add('strength-strong');
            strengthText.textContent = 'Password strength: Strong';
            strengthText.style.color = '#27ae60';
        } else if (strength >= 3) {
            strengthBar.classList.add('strength-good');
            strengthText.textContent = 'Password strength: Good';
            strengthText.style.color = '#f1c40f';
        } else if (strength >= 2) {
            strengthBar.classList.add('strength-fair');
            strengthText.textContent = 'Password strength: Fair';
            strengthText.style.color = '#f39c12';
        } else if (strength >= 1) {
            strengthBar.classList.add('strength-weak');
            strengthText.textContent = 'Password strength: Weak';
            strengthText.style.color = '#e74c3c';
        } else {
            strengthText.textContent = 'Password strength: None';
            strengthText.style.color = 'rgba(255, 255, 255, 0.8)';
        }

        checkPasswordMatch();
    });

    // Password confirmation check
    function checkPasswordMatch() {
        if (confirmPasswordInput.value && passwordInput.value !== confirmPasswordInput.value) {
            confirmPasswordInput.classList.add('is-invalid');
            confirmPasswordInput.classList.remove('is-valid');
            return false;
        } else if (confirmPasswordInput.value && passwordInput.value === confirmPasswordInput.value) {
            confirmPasswordInput.classList.add('is-valid');
            confirmPasswordInput.classList.remove('is-invalid');
            return true;
        } else {
            confirmPasswordInput.classList.remove('is-valid', 'is-invalid');
            return true;
        }
    }

    confirmPasswordInput.addEventListener('input', checkPasswordMatch);

    // Form submission
    form.addEventListener('submit', function (e) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Creating User...';
    });
});
