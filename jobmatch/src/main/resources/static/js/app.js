// JobMatch: підтвердження незворотних дій і показ/приховування форм.
document.addEventListener('submit', function (e) {
  var msg = e.target.getAttribute('data-confirm');
  if (msg && !window.confirm(msg)) {
    e.preventDefault();
  }
});
document.addEventListener('click', function (e) {
  var t = e.target.closest('[data-toggle]');
  if (!t) return;
  var el = document.getElementById(t.getAttribute('data-toggle'));
  if (el) {
    el.hidden = !el.hidden;
    t.setAttribute('aria-expanded', String(!el.hidden));
    if (!el.hidden) { var f = el.querySelector('input,select,textarea'); if (f) f.focus(); }
  }
});
document.addEventListener('change', function (e) {
  if (e.target.name === 'role') {
    document.querySelectorAll('[data-role]').forEach(function (b) {
      b.hidden = b.getAttribute('data-role') !== e.target.value;
    });
  }
});
// Лічильник символів для полів з обмеженням довжини (напр. «Коментар для компанії», ≤ 500).
document.querySelectorAll('textarea[maxlength]').forEach(function (ta) {
  var max = ta.getAttribute('maxlength');
  var out = document.createElement('div');
  out.className = 'hint counter';
  out.setAttribute('aria-live', 'polite');
  var upd = function () { out.textContent = ta.value.length + ' / ' + max + ' символів'; };
  ta.insertAdjacentElement('afterend', out);
  ta.addEventListener('input', upd);
  upd();
});
// Після помилки валідації фокус переходить на перше поле з помилкою.
(function () {
  var bad = document.querySelector('input.invalid, select.invalid, textarea.invalid');
  if (bad) { bad.focus(); }
})();
