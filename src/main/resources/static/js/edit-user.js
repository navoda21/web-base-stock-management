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

    const form = document.getElementById('editUserForm');
    const submitBtn = document.getElementById('submitBtn');

    // Form submission
    if (form && submitBtn) {
        form.addEventListener('submit', function (e) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Updating User...';
        });
    }

    // Add smooth animations to form groups
    const formGroups = document.querySelectorAll('.form-group');
    formGroups.forEach((group, index) => {
        group.style.animationDelay = `${index * 0.1}s`;
    });
});
