/* VNSO CloudStack docs — catalog filtering */
(function () {
  document.addEventListener('DOMContentLoaded', function () {
    var search = document.getElementById('svc-search');
    var chips  = document.querySelectorAll('.chip');
    var cards  = document.querySelectorAll('.svc-card');
    var count  = document.getElementById('svc-count');
    if (!cards.length) return;
    var activeCat = 'all';

    function apply() {
      var q = (search && search.value || '').trim().toLowerCase();
      var visible = 0;
      cards.forEach(function (c) {
        var cat  = c.getAttribute('data-cat') || '';
        var text = (c.textContent || '').toLowerCase();
        var matchCat = activeCat === 'all' || cat === activeCat;
        var matchQ   = !q || text.indexOf(q) !== -1;
        var hide = !(matchCat && matchQ);
        c.classList.toggle('hidden', hide);
        if (!hide) visible++;
      });
      if (count) count.textContent = visible + (visible === 1 ? ' mục' : ' mục');
    }

    if (search) search.addEventListener('input', apply);
    chips.forEach(function (ch) {
      ch.addEventListener('click', function () {
        chips.forEach(function (x) { x.classList.remove('active'); });
        ch.classList.add('active');
        activeCat = ch.getAttribute('data-cat') || 'all';
        apply();
      });
    });
    apply();
  });
})();
