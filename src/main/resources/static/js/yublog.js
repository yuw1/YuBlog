/* YuBlog — small vanilla-JS layer. No jQuery, no framework. */
(function () {
    'use strict';

    var THEME_KEY = 'yublog-theme';
    var root = document.documentElement;

    /* ------------------------------------------------------------ theme */
    function currentTheme() {
        var explicit = root.getAttribute('data-theme');
        if (explicit) {
            return explicit;
        }
        return window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }

    function applyTheme(theme, persist) {
        root.setAttribute('data-theme', theme);
        if (persist) {
            try {
                localStorage.setItem(THEME_KEY, theme);
            } catch (e) {
                /* private mode: ignore */
            }
        }
        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) {
            meta.setAttribute('content', theme === 'light' ? '#f5f5fa' : '#08080d');
        }
        document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
            btn.setAttribute('aria-pressed', theme === 'dark' ? 'true' : 'false');
            btn.setAttribute('title', theme === 'dark' ? '切换到浅色' : '切换到深色');
        });
    }

    document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            applyTheme(currentTheme() === 'dark' ? 'light' : 'dark', true);
        });
    });

    /* ----------------------------------------------------------- drawer */
    var drawer = document.querySelector('[data-drawer]');
    if (drawer) {
        var lastFocus = null;

        function openDrawer() {
            lastFocus = document.activeElement;
            drawer.setAttribute('data-open', 'true');
            document.body.style.overflow = 'hidden';
            var close = drawer.querySelector('[data-drawer-close]');
            if (close) {
                close.focus();
            }
        }

        function closeDrawer() {
            drawer.setAttribute('data-open', 'false');
            document.body.style.overflow = '';
            if (lastFocus) {
                lastFocus.focus();
            }
        }

        document.querySelectorAll('[data-drawer-open]').forEach(function (btn) {
            btn.addEventListener('click', function (event) {
                event.preventDefault();
                openDrawer();
            });
        });
        drawer.querySelectorAll('[data-drawer-close], .drawer__backdrop').forEach(function (el) {
            el.addEventListener('click', closeDrawer);
        });
        drawer.querySelectorAll('a[href]').forEach(function (el) {
            el.addEventListener('click', closeDrawer);
        });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && drawer.getAttribute('data-open') === 'true') {
                closeDrawer();
            }
        });
    }

    /* -------------------------------------------------- dropdown menus */
    document.querySelectorAll('.menu').forEach(function (menu) {
        document.addEventListener('click', function (event) {
            if (menu.hasAttribute('open') && !menu.contains(event.target)) {
                menu.removeAttribute('open');
            }
        });
        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                menu.removeAttribute('open');
            }
        });
    });

    /* -------------------------------------------------- reading progress */
    var bar = document.querySelector('[data-progress]');
    var body = document.querySelector('[data-progress-target]');
    if (bar && body) {
        var update = function () {
            var rect = body.getBoundingClientRect();
            var total = rect.height - window.innerHeight * 0.4;
            var seen = Math.min(Math.max(-rect.top + window.innerHeight * 0.2, 0), Math.max(total, 1));
            bar.style.width = (seen / Math.max(total, 1) * 100).toFixed(2) + '%';
        };
        update();
        window.addEventListener('scroll', update, {passive: true});
        window.addEventListener('resize', update);
    }

    /* --------------------------------------------------------- to-top */
    var toTop = document.querySelector('[data-to-top]');
    if (toTop) {
        var toggleToTop = function () {
            toTop.setAttribute('data-show', window.scrollY > 480 ? 'true' : 'false');
        };
        toggleToTop();
        window.addEventListener('scroll', toggleToTop, {passive: true});
        toTop.addEventListener('click', function (event) {
            event.preventDefault();
            window.scrollTo({top: 0, behavior: 'smooth'});
        });
    }

    /* --------------------------------------------------- csrf helpers */
    function csrfHeaders() {
        var token = document.querySelector('meta[name="csrf-token"]');
        var header = document.querySelector('meta[name="csrf-header"]');
        if (!token || !header) {
            return {};
        }
        var headers = {};
        headers[header.getAttribute('content')] = token.getAttribute('content');
        return headers;
    }

    /* ------------------------------------------- apply to be an admin */
    var applyBtn = document.querySelector('[data-apply-admin]');
    if (applyBtn) {
        applyBtn.addEventListener('click', function () {
            applyBtn.disabled = true;
            fetch(applyBtn.getAttribute('data-apply-admin'), {
                method: 'POST',
                headers: csrfHeaders(),
                credentials: 'same-origin'
            }).then(function (response) {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            }).then(function (payload) {
                if (payload && payload.state === 'success') {
                    applyBtn.textContent = '已申请，等待审核';
                    applyBtn.classList.remove('btn--primary');
                    applyBtn.classList.add('badge');
                } else {
                    applyBtn.disabled = false;
                }
            }).catch(function () {
                applyBtn.disabled = false;
                applyBtn.textContent = '申请失败，请重试';
            });
        });
    }

    /* ------------------------------------------------- editor preview */
    var editorForm = document.querySelector('[data-editor]');
    if (editorForm) {
        var titleInput = editorForm.querySelector('[name="title-text"]');
        var authorInput = editorForm.querySelector('[name="author-text"]');
        var bodyInput = editorForm.querySelector('[name="content-text"]');
        var abstractInput = editorForm.querySelector('[name="article-abstract"]');
        var previewTitle = document.querySelector('[data-preview-title]');
        var previewAuthor = document.querySelector('[data-preview-author]');
        var previewBody = document.querySelector('[data-preview-body]');
        var previewCatalog = document.querySelector('[data-preview-catalog]');

        var render = function () {
            if (previewTitle) {
                previewTitle.textContent = (titleInput && titleInput.value.trim()) || '未命名文章';
            }
            if (previewAuthor && authorInput) {
                previewAuthor.textContent = authorInput.value;
            }
            if (previewBody && bodyInput) {
                // the author is previewing their own markup; the server sanitizes it on publish
                previewBody.innerHTML = bodyInput.value;
            }
            if (previewCatalog && abstractInput && abstractInput.value.trim()) {
                previewCatalog.textContent = abstractInput.value.trim();
            }
        };
        ['input', 'change', 'keyup'].forEach(function (evt) {
            editorForm.addEventListener(evt, render);
        });
        render();
    }

    /* -------------------------------------------------- catalog picker */
    var catalogDialog = document.querySelector('[data-catalog-dialog]');
    if (catalogDialog) {
        var catalogInput = document.querySelector('#article-catalog-id');
        var catalogLabel = document.querySelector('#article-catalog');

        document.querySelectorAll('[data-catalog-open]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                if (typeof catalogDialog.showModal === 'function') {
                    catalogDialog.showModal();
                } else {
                    catalogDialog.setAttribute('open', 'open');
                }
            });
        });
        document.querySelectorAll('[data-catalog-close]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                if (typeof catalogDialog.close === 'function') {
                    catalogDialog.close();
                } else {
                    catalogDialog.removeAttribute('open');
                }
            });
        });
        catalogDialog.querySelectorAll('[data-catalog-id]').forEach(function (item) {
            item.addEventListener('click', function (event) {
                event.preventDefault();
                if (catalogInput) {
                    catalogInput.value = item.getAttribute('data-catalog-id');
                }
                if (catalogLabel) {
                    catalogLabel.value = item.getAttribute('data-catalog-name');
                }
                if (typeof catalogDialog.close === 'function') {
                    catalogDialog.close();
                }
            });
        });
    }

    /* ------------------------------------------------ password confirm */
    document.querySelectorAll('[data-password-form]').forEach(function (form) {
        var pwd = form.querySelector('[name="pwd"]');
        var confirm = form.querySelector('[name="pwd_confirm"]');
        var submit = form.querySelector('[type="submit"]');
        var tip = form.querySelector('[data-password-tip]');
        if (!pwd || !confirm) {
            return;
        }
        var check = function () {
            var same = pwd.value !== '' && pwd.value === confirm.value;
            if (tip) {
                tip.textContent = pwd.value === '' || confirm.value === ''
                    ? ''
                    : (same ? '两次输入一致' : '两次输入不一致');
                tip.className = same ? 'mono soft' : 'mono';
                tip.style.color = same ? '' : 'var(--danger)';
            }
            if (submit) {
                submit.disabled = !(same && pwd.value.length >= 6);
            }
        };
        [pwd, confirm].forEach(function (input) {
            input.addEventListener('input', check);
        });
        check();
    });

    /* ------------------------------------------------------ autogrow */
    document.querySelectorAll('[data-autogrow]').forEach(function (area) {
        var grow = function () {
            area.style.height = 'auto';
            area.style.height = Math.max(area.scrollHeight, 220) + 'px';
        };
        area.addEventListener('input', grow);
        grow();
    });

    // initial paint follows the system preference; only an explicit click is persisted
    applyTheme(currentTheme(), false);
})();