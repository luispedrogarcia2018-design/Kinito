// ===== KINITO: conecta el frontend con el backend (Spring Boot + H2) =====

const API_PRODUCTOS = '/api/productos';
const API_CONTACTO = '/api/contacto';
const API_PEDIDOS = '/api/pedidos';

let carrito = []; // { productoId, nombre, precio, cantidad }

// --- Cargar catálogo desde la base de datos ---
async function cargarProductos() {
  const estado = document.getElementById('productos-estado');
  const grid = document.getElementById('productos-grid');

  try {
    const res = await fetch(API_PRODUCTOS);
    if (!res.ok) throw new Error('Respuesta no válida del servidor');
    const productos = await res.json();

    if (productos.length === 0) {
      estado.textContent = 'Todavía no hay prendas en el inventario.';
      return;
    }

    estado.classList.add('oculto');
    grid.innerHTML = productos.map(renderProductoCard).join('');
    grid.querySelectorAll('.producto-agregar').forEach(btn => {
      btn.addEventListener('click', () => agregarAlCarrito(btn.dataset, btn));
    });
    initScrollReveal(); // vuelve a observar las tarjetas recién insertadas
  } catch (err) {
    estado.textContent = 'No se pudo cargar el inventario. ¿Ya está corriendo el backend?';
    estado.classList.add('productos-error');
    console.error(err);
  }
}

function renderProductoCard(p) {
  const sinStock = p.stock <= 0;
  const categoriaNombre = p.categoria ? p.categoria.nombre : '';
  return `
    <article class="producto-card reveal">
      <div class="producto-punch" aria-hidden="true"></div>
      <div class="producto-img-wrap">
        <img src="${p.imagenUrl || ''}" alt="${p.nombre}" loading="lazy">
        <span class="ver-detalle">Ver detalle</span>
      </div>
      <p class="producto-categoria">${categoriaNombre}</p>
      <h3 class="producto-nombre">${p.nombre}</h3>
      <p class="producto-desc">${p.descripcion || ''}</p>
      <div class="producto-footer">
        <span class="producto-precio">Q${Number(p.precio).toFixed(2)}</span>
        <span class="producto-stock ${sinStock ? 'agotado' : ''}">
          ${sinStock ? 'Agotado' : 'Talla ' + (p.talla || '-')}
        </span>
      </div>
      <button class="btn btn-outline producto-agregar" ${sinStock ? 'disabled' : ''}
        data-id="${p.id}" data-nombre="${p.nombre}" data-precio="${p.precio}">
        ${sinStock ? 'Agotado' : 'Agregar al pedido'}
      </button>
    </article>
  `;
}

// --- Carrito ---
function agregarAlCarrito(data, btnEl) {
  const id = Number(data.id);
  const existente = carrito.find(item => item.productoId === id);
  if (existente) {
    existente.cantidad += 1;
  } else {
    carrito.push({ productoId: id, nombre: data.nombre, precio: Number(data.precio), cantidad: 1 });
  }
  renderCarrito();

  // Feedback visual: el botón confirma brevemente que se agregó
  if (btnEl) {
    const textoOriginal = btnEl.textContent;
    btnEl.textContent = '✓ Agregado';
    btnEl.classList.add('agregado');
    btnEl.disabled = true;
    setTimeout(() => {
      btnEl.textContent = textoOriginal;
      btnEl.classList.remove('agregado');
      btnEl.disabled = false;
    }, 900);
  }
}

function quitarDelCarrito(id) {
  carrito = carrito.filter(item => item.productoId !== id);
  renderCarrito();
}

function cambiarCantidad(id, delta) {
  const item = carrito.find(i => i.productoId === id);
  if (!item) return;
  item.cantidad += delta;
  if (item.cantidad <= 0) {
    quitarDelCarrito(id);
  } else {
    renderCarrito();
  }
}

function renderCarrito() {
  const vacio = document.getElementById('carrito-vacio');
  const lista = document.getElementById('carrito-lista');
  const totalEl = document.getElementById('carrito-total');

  if (carrito.length === 0) {
    vacio.style.display = 'block';
    lista.innerHTML = '';
    totalEl.textContent = '';
    return;
  }

  vacio.style.display = 'none';
  lista.innerHTML = carrito.map(item => `
    <li class="carrito-item">
      <span class="carrito-item-nombre">${item.nombre}</span>
      <span class="carrito-item-controles">
        <button type="button" class="carrito-btn" data-accion="menos" data-id="${item.productoId}">−</button>
        <span>${item.cantidad}</span>
        <button type="button" class="carrito-btn" data-accion="mas" data-id="${item.productoId}">+</button>
      </span>
      <span class="carrito-item-precio">Q${(item.precio * item.cantidad).toFixed(2)}</span>
      <button type="button" class="carrito-btn carrito-quitar" data-accion="quitar" data-id="${item.productoId}">×</button>
    </li>
  `).join('');

  lista.querySelectorAll('.carrito-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = Number(btn.dataset.id);
      if (btn.dataset.accion === 'mas') cambiarCantidad(id, 1);
      if (btn.dataset.accion === 'menos') cambiarCantidad(id, -1);
      if (btn.dataset.accion === 'quitar') quitarDelCarrito(id);
    });
  });

  const total = carrito.reduce((sum, i) => sum + i.precio * i.cantidad, 0);
  totalEl.textContent = `Total: Q${total.toFixed(2)}`;
}

// --- Enviar pedido (carrito) ---
function initFormularioPedido() {
  const form = document.getElementById('form-pedido');
  const respuesta = document.getElementById('pedido-respuesta');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (carrito.length === 0) {
      respuesta.textContent = 'Agrega al menos una prenda antes de confirmar.';
      respuesta.className = 'form-respuesta error';
      return;
    }

    respuesta.textContent = 'Enviando...';
    respuesta.className = 'form-respuesta';

    const datos = {
      nombre: document.getElementById('ped-nombre').value.trim(),
      email: document.getElementById('ped-email').value.trim(),
      telefono: document.getElementById('ped-telefono').value.trim(),
      direccion: document.getElementById('ped-direccion').value.trim(),
      items: carrito.map(i => ({ productoId: i.productoId, cantidad: i.cantidad })),
    };

    try {
      const res = await fetch(API_PEDIDOS, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos),
      });

      if (!res.ok) {
        const errText = await res.text().catch(() => '');
        throw new Error(errText || 'No se pudo crear el pedido');
      }

      respuesta.textContent = '¡Pedido confirmado! Te contactamos para coordinar el pago y la entrega.';
      respuesta.classList.add('ok');
      form.reset();
      carrito = [];
      renderCarrito();
      cargarProductos(); // refresca stock mostrado
    } catch (err) {
      respuesta.textContent = 'Algo falló al enviar el pedido: ' + err.message;
      respuesta.classList.add('error');
      console.error(err);
    }
  });
}

// --- Enviar formulario de contacto (preguntas generales) ---
function initFormularioContacto() {
  const form = document.getElementById('form-contacto');
  const respuesta = document.getElementById('form-respuesta');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    respuesta.textContent = 'Enviando...';
    respuesta.className = 'form-respuesta';

    const datos = {
      nombre: form.nombre.value.trim(),
      email: form.email.value.trim(),
      telefono: form.telefono.value.trim(),
      mensaje: form.mensaje.value.trim(),
    };

    try {
      const res = await fetch(API_CONTACTO, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos),
      });

      if (!res.ok) throw new Error('No se pudo enviar el mensaje');

      respuesta.textContent = '¡Listo! Ya nos llegó tu mensaje, te contestamos pronto.';
      respuesta.classList.add('ok');
      form.reset();
    } catch (err) {
      respuesta.textContent = 'Algo falló al enviar. Intenta de nuevo en un momento.';
      respuesta.classList.add('error');
      console.error(err);
    }
  });
}

// --- Pestañas ---
function initTabs() {
  const botones = document.querySelectorAll('.tab-btn');
  botones.forEach(btn => {
    btn.addEventListener('click', () => {
      botones.forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
      btn.classList.add('active');
      document.getElementById('panel-' + btn.dataset.tab).classList.add('active');
    });
  });
}

// ===== Nav con glassmorphism al hacer scroll =====
function initNavScroll() {
  const rail = document.querySelector('.rail');
  if (!rail) return;
  const onScroll = () => {
    rail.classList.toggle('scrolled', window.scrollY > 12);
  };
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();
}

// ===== Aparición suave (fade-in-up) al hacer scroll =====
let revealObserver;
function initScrollReveal() {
  if (!revealObserver) {
    revealObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('in-view');
          revealObserver.unobserve(entry.target);
        }
      });
    }, { threshold: 0.12 });
  }
  document.querySelectorAll('.reveal:not(.in-view)').forEach(el => revealObserver.observe(el));
}

document.addEventListener('DOMContentLoaded', () => {
  cargarProductos();
  initFormularioContacto();
  initFormularioPedido();
  initTabs();
  renderCarrito();
  initNavScroll();
  initScrollReveal();
});
