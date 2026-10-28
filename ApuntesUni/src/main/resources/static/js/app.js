/**
 * Apuntes UNI — Interactividad Vanilla JS
 */

document.addEventListener('DOMContentLoaded', () => {
  // Manejo de Modales genéricos
  window.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add('active');
      document.body.style.overflow = 'hidden';
    }
  };

  window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove('active');
      document.body.style.overflow = '';
    }
  };

  // Cerrar modal al hacer click en overlay o data-close
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay || e.target.closest('[data-close-modal]')) {
        overlay.classList.remove('active');
        document.body.style.overflow = '';
      }
    });
  });

  // Cerrar con Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      document.querySelectorAll('.modal-overlay.active').forEach(modal => {
        modal.classList.remove('active');
      });
      const drawer = document.getElementById('reportDrawer');
      const backdrop = document.getElementById('drawerBackdrop');
      if (drawer) drawer.classList.remove('active');
      if (backdrop) backdrop.classList.remove('active');
      document.body.style.overflow = '';
    }
  });

  // Sistema de Tabs
  document.querySelectorAll('[data-tabs]').forEach(tabContainer => {
    const tabs = tabContainer.querySelectorAll('.tab-btn');
    tabs.forEach(tab => {
      tab.addEventListener('click', () => {
        tabs.forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const targetId = tab.getAttribute('data-tab-target');
        if (targetId) {
          const contentArea = tabContainer.closest('section') || document;
          contentArea.querySelectorAll('[data-tab-content]').forEach(c => {
            c.style.display = (c.id === targetId) ? '' : 'none';
          });
        }
      });
    });
  });

  // Selector de semestre en Onboarding o Perfil
  document.querySelectorAll('.semesters-grid .semester-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const parent = btn.closest('.semesters-grid');
      parent.querySelectorAll('.semester-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  // Toggle de visibilidad de perfil en configuracion.html
  document.querySelectorAll('.toggle-group-buttons .toggle-group-btn').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const parent = btn.closest('.toggle-group-buttons');
      parent.querySelectorAll('.toggle-group-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
    });
  });

  // Calificación de estrellas interactiva (archivo.html)
  const starContainer = document.getElementById('starRatingInput');
  if (starContainer) {
    const stars = starContainer.querySelectorAll('span');
    stars.forEach((star, index) => {
      star.addEventListener('mouseenter', () => {
        stars.forEach((s, i) => {
          s.style.color = (i <= index) ? '#f59e0b' : '#cbd5e1';
          s.textContent = (i <= index) ? '★' : '☆';
        });
      });
      star.addEventListener('click', () => {
        stars.forEach((s, i) => {
          s.classList.toggle('selected', i <= index);
          s.textContent = (i <= index) ? '★' : '☆';
          s.style.color = (i <= index) ? '#f59e0b' : '#cbd5e1';
        });
      });
    });
    starContainer.addEventListener('mouseleave', () => {
      stars.forEach(s => {
        if (s.classList.contains('selected')) {
          s.style.color = '#f59e0b';
          s.textContent = '★';
        } else {
          s.style.color = '#cbd5e1';
          s.textContent = '☆';
        }
      });
    });
  }

  // Toggle Inscribirme / Inscrito (mis-materias.html)
  document.querySelectorAll('.btn-enroll-toggle').forEach(btn => {
    btn.addEventListener('click', () => {
      const isInscribed = btn.classList.contains('btn-primary');
      if (isInscribed) {
        btn.classList.remove('btn-primary');
        btn.classList.add('btn-outline');
        btn.textContent = 'Inscribirme';
      } else {
        btn.classList.remove('btn-outline');
        btn.classList.add('btn-primary');
        btn.innerHTML = '✓ Inscrito';
      }
    });
  });

  // Búsqueda en Header
  document.querySelectorAll('.header-search input').forEach(input => {
    input.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        window.location.href = 'buscar.html?q=' + encodeURIComponent(input.value || 'cálculo');
      }
    });
  });
});
