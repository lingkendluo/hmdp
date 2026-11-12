/* ============================================================
   黑马点评 · 前端公共逻辑
   ============================================================ */

const HM = {
    token: localStorage.getItem('hmdp_token') || '',
    user: null,
    base: '',
};

try {
    HM.user = JSON.parse(localStorage.getItem('hmdp_user') || 'null');
} catch (e) {
    HM.user = null;
}

/* ---------------- 图标 ---------------- */

const ICON = {
    home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M3 10.7 12 3.4l9 7.3"/><path d="M5.6 9.6V20.2h12.8V9.6"/><path d="M9.7 20.2v-5.1h4.6v5.1"/></svg>',
    near: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M12 21s6.5-6.1 6.5-10.4A6.5 6.5 0 0 0 5.5 10.6C5.5 14.9 12 21 12 21z"/><circle cx="12" cy="10.4" r="2.4"/></svg>',
    heart: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20.2s-7.6-4.6-7.6-9.7A4.3 4.3 0 0 1 12 7.4a4.3 4.3 0 0 1 7.6 3.1c0 5.1-7.6 9.7-7.6 9.7z"/></svg>',
    heartFill: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 20.6s-7.9-4.8-7.9-10.1A4.5 4.5 0 0 1 12 7.2a4.5 4.5 0 0 1 7.9 3.3c0 5.3-7.9 10.1-7.9 10.1z"/></svg>',
    ticket: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M3.5 8.5V6.8h17v1.7a2.6 2.6 0 0 0 0 5.2v1.7h-17v-1.7a2.6 2.6 0 0 0 0-5.2z"/><path d="M9.4 8.7v6.8" stroke-dasharray="1.6 2"/></svg>',
    user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="8.4" r="3.7"/><path d="M4.9 20.2c.7-3.5 3.6-5.6 7.1-5.6s6.4 2.1 7.1 5.6"/></svg>',
    search: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linecap="round"><circle cx="11" cy="11" r="6.6"/><path d="m16 16 4.2 4.2"/></svg>',
    back: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><path d="m14.5 5-7 7 7 7"/></svg>',
    star: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="m12 3.6 2.6 5.3 5.8.9-4.2 4.1 1 5.8-5.2-2.7-5.2 2.7 1-5.8L3.6 9.8l5.8-.9z"/></svg>',
    comment: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"><path d="M20.4 12.3c0 3.9-3.8 7-8.4 7-1 0-2-.1-2.9-.4l-4.5 1.6 1.5-3.6a6.6 6.6 0 0 1-2.5-4.6c0-3.9 3.8-7 8.4-7s8.4 3.1 8.4 7z"/></svg>',
    plus: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"><path d="M12 5.5v13M5.5 12h13"/></svg>',
    clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round"><circle cx="12" cy="12" r="8.3"/><path d="M12 7.6V12l3 1.8"/></svg>',
};

/* ---------------- 网络请求 ---------------- */

async function api(path, options = {}) {
    const {method = 'GET', body, quiet = false} = options;
    const headers = {};
    if (HM.token) headers['authorization'] = HM.token;

    const init = {method, headers};
    if (body !== undefined) {
        headers['Content-Type'] = 'application/json';
        init.body = JSON.stringify(body);
    }

    let res;
    try {
        res = await fetch(HM.base + path, init);
    } catch (e) {
        toast('网络异常，请检查后端服务');
        throw e;
    }

    if (res.status === 401) {
        HM.clearAuth();
        if (!quiet) {
            toast('登录已过期，请重新登录');
            setTimeout(() => goLogin(), 700);
        }
        throw new Error('unauthorized');
    }

    const data = await res.json().catch(() => ({success: false, errorMsg: '响应解析失败'}));
    if (!data.success) {
        if (!quiet) toast(data.errorMsg || '请求失败');
        throw new Error(data.errorMsg || 'request failed');
    }
    return data;
}

HM.clearAuth = function () {
    HM.token = '';
    HM.user = null;
    localStorage.removeItem('hmdp_token');
    localStorage.removeItem('hmdp_user');
};

HM.setAuth = function (token, user) {
    HM.token = token;
    HM.user = user;
    localStorage.setItem('hmdp_token', token);
    localStorage.setItem('hmdp_user', JSON.stringify(user));
};

HM.isLogin = function () {
    return !!HM.token;
};

HM.logout = async function () {
    try {
        await api('/user/logout', {method: 'POST', quiet: true});
    } catch (e) {
        /* ignore */
    }
    HM.clearAuth();
    location.href = '/index.html';
};

/* ---------------- 工具 ---------------- */

function goLogin() {
    const back = encodeURIComponent(location.pathname + location.search);
    location.href = '/login.html?redirect=' + back;
}

/** 需要登录：未登录直接跳转登录页 */
function requireLogin() {
    if (!HM.isLogin()) {
        goLogin();
        return false;
    }
    return true;
}

function toast(msg, duration = 1800) {
    let el = document.getElementById('toast');
    if (!el) {
        el = document.createElement('div');
        el.id = 'toast';
        document.body.appendChild(el);
    }
    el.textContent = msg;
    el.classList.add('show');
    clearTimeout(el._t);
    el._t = setTimeout(() => el.classList.remove('show'), duration);
}

function esc(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/** 分 -> 元（去掉多余的 0） */
function yuan(fen) {
    if (fen === null || fen === undefined) return '0';
    const v = Number(fen) / 100;
    return Number.isInteger(v) ? String(v) : v.toFixed(2).replace(/0$/, '');
}

function firstImg(images) {
    if (!images) return '/imgs/shop/1-1.svg';
    const arr = String(images).split(',').filter(Boolean);
    return arr.length ? arr[0] : '/imgs/shop/1-1.svg';
}

function imgArr(images) {
    if (!images) return [];
    return String(images).split(',').filter(Boolean);
}

function timeAgo(iso) {
    if (!iso) return '';
    const t = new Date(iso).getTime();
    if (Number.isNaN(t)) return '';
    const diff = Date.now() - t;
    const min = 60 * 1000, hour = 60 * min, day = 24 * hour;
    if (diff < min) return '刚刚';
    if (diff < hour) return Math.floor(diff / min) + ' 分钟前';
    if (diff < day) return Math.floor(diff / hour) + ' 小时前';
    if (diff < 30 * day) return Math.floor(diff / day) + ' 天前';
    const d = new Date(t);
    return `${d.getMonth() + 1}月${d.getDate()}日`;
}

function scoreText(score) {
    if (!score) return '0.0';
    return (score / 10).toFixed(1);
}

/** 距离（米）格式化 */
function distanceText(m) {
    if (m === null || m === undefined) return '';
    if (m < 10) return '就在附近';
    if (m < 1000) return Math.round(m) + 'm';
    return (m / 1000).toFixed(1) + 'km';
}

/** 从 URL 取参数 */
function query(name, def) {
    const v = new URLSearchParams(location.search).get(name);
    return v === null ? def : v;
}

/* ---------------- 底部导航 ---------------- */

function renderTabbar(active) {
    const items = [
        {key: 'home', href: '/index.html', icon: ICON.home, label: '首页'},
        {key: 'near', href: '/near.html', icon: ICON.near, label: '附近'},
        {key: 'feed', href: '/feed.html', icon: ICON.heart, label: '关注'},
        {key: 'orders', href: '/orders.html', icon: ICON.ticket, label: '订单'},
        {key: 'me', href: '/me.html', icon: ICON.user, label: '我的'},
    ];
    const nav = document.createElement('nav');
    nav.className = 'tabbar';
    nav.innerHTML = items.map(it => `
        <a href="${it.href}" class="${it.key === active ? 'active' : ''}">
            <span class="ico">${it.icon}</span>
            <span>${it.label}</span>
        </a>`).join('');
    document.querySelector('.app').appendChild(nav);
}

/* ---------------- 无限滚动 ---------------- */

/**
 * 创建一个分页加载器
 * @param {Object} opt
 * @param {Function} opt.fetch  (page) => Promise<{list, total, page}>
 * @param {Function} opt.render (list, isFirst) => void
 * @param {Element} opt.sentinel 触底元素
 */
function createPager({fetch, render, sentinel, emptyEl}) {
    let page = 0;
    let total = Infinity;
    let loading = false;
    let done = false;

    async function load() {
        if (loading || done) return;
        loading = true;
        sentinel.textContent = '加载中…';
        try {
            const res = await fetch(page + 1);
            const list = res.list || [];
            page += 1;
            if (page === 1) {
                if (!list.length && emptyEl) emptyEl.style.display = 'block';
            }
            render(list, page === 1);
            const loaded = page * (res.size || list.length || 5);
            if (list.length === 0 || (res.total !== undefined && loaded >= res.total)) {
                done = true;
                sentinel.textContent = page === 1 ? '' : '— 没有更多了 —';
            } else {
                sentinel.textContent = '';
            }
            // 内容不足一屏时继续加载，避免出现大片空白
            if (!done && document.body.scrollHeight <= window.innerHeight + 40) {
                setTimeout(load, 60);
            }
        } catch (e) {
            sentinel.textContent = '加载失败，点击重试';
            sentinel.onclick = () => {
                sentinel.onclick = null;
                load();
            };
        } finally {
            loading = false;
        }
    }

    const io = new IntersectionObserver(entries => {
        if (entries[0].isIntersecting) load();
    }, {rootMargin: '120px'});
    io.observe(sentinel);

    return {load, reset: () => { page = 0; done = false; }, get page() { return page; }};
}

/* ---------------- 常用片段 ---------------- */

function shopCardHtml(s) {
    const dist = (s.distance !== null && s.distance !== undefined)
        ? `<span class="distance">${distanceText(s.distance)}</span>` : '';
    return `
    <a class="shop-card" href="/shop.html?id=${s.id}">
        <div class="thumb"><img class="cover" src="${firstImg(s.images)}" alt=""></div>
        <div class="body">
            <div>
                <div class="nm">${esc(s.name)}</div>
                <div class="meta">
                    <span class="score"><span class="s-ico">${ICON.star}</span>${scoreText(s.score)}</span>
                    <span>${esc(s.area || '')}</span>
                    <span>人均 ¥${s.avgPrice}</span>
                </div>
            </div>
            <div class="row-b">
                <span class="tiny">已售 ${s.sold > 10000 ? (s.sold / 10000).toFixed(1) + '万' : s.sold}</span>
                ${dist}
            </div>
        </div>
    </a>`;
}

function blogCardHtml(b) {
    return `
    <a class="blog-card" href="/blog.html?id=${b.id}">
        <div class="bimg"><img class="cover" src="${firstImg(b.images)}" alt=""></div>
        <div class="bbody">
            <div class="btitle">${esc(b.title)}</div>
            <div class="bdesc">${esc(b.content || '')}</div>
            <div class="bmeta">
                <img src="${b.userIcon || '/imgs/avatar/1.svg'}" alt="">
                <span class="name">${esc(b.userName || '匿名用户')}</span>
                <span>· ${timeAgo(b.createTime)}</span>
                <span class="likes">${ICON.heartFill.replace('currentColor', '#ff5a3c')} ${b.liked || 0}</span>
            </div>
        </div>
    </a>`;
}

/** 顶部返回按钮绑定 */
function bindBack() {
    const el = document.querySelector('.topbar .back, .shop-hero .nav .back-btn');
    if (el) {
        el.addEventListener('click', () => {
            if (document.referrer && history.length > 1) {
                history.back();
            } else {
                location.href = '/index.html';
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', bindBack);
