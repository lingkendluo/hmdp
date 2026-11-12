# -*- coding: utf-8 -*-
"""
生成 hmdp 演示资源：
1. 店铺封面 SVG（渐变海报风格）
2. 用户头像 SVG
3. sql/data.sql（分类 / 店铺 / 用户 / 券 / 笔记 / 关注）
"""
import os
import random
from html import escape

random.seed(20261107)

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMG_SHOP = os.path.join(BASE, "src", "main", "resources", "static", "imgs", "shop")
IMG_AVATAR = os.path.join(BASE, "src", "main", "resources", "static", "imgs", "avatar")
SQL_DIR = os.path.join(BASE, "sql")

for d in (IMG_SHOP, IMG_AVATAR, SQL_DIR):
    os.makedirs(d, exist_ok=True)

# ---------------- 分类 ----------------
# id, 名称, emoji, 排序
TYPES = [
    (1, "美食", "🍜", 1),
    (2, "娱乐", "🎤", 2),
    (3, "酒店", "🏨", 3),
    (4, "丽人", "💅", 4),
    (5, "健身", "🏋️", 5),
    (6, "按摩足疗", "💆", 6),
    (7, "亲子", "🎠", 7),
    (8, "电影", "🎬", 8),
    (9, "美发", "✂️", 9),
    (10, "酒吧", "🍺", 10),
]

# 类型配色（渐变起止）
PALETTE = {
    1: ("#FF7A45", "#FFB020"),
    2: ("#6D5BF6", "#A78BFA"),
    3: ("#0EA5E9", "#38BDF8"),
    4: ("#EC4899", "#F9A8D4"),
    5: ("#059669", "#34D399"),
    6: ("#0D9488", "#2DD4BF"),
    7: ("#F59E0B", "#FCD34D"),
    8: ("#1E293B", "#475569"),
    9: ("#9333EA", "#C084FC"),
    10: ("#7C2D12", "#B45309"),
}

# 店铺：名称, 类型, 商圈, 地址, 经度, 纬度, 均价, 销量, 评论数, 评分(乘10), 营业时间, 短名
SHOPS = [
    ("海底捞火锅(三里屯店)", 1, "三里屯", "朝阳区三里屯路19号院3层", 116.4551, 39.9345, 128, 48210, 23188, 48, "10:00-次日05:00", "海底捞"),
    ("很久以前羊肉串(工体店)", 1, "工人体育场", "朝阳区工人体育场北路甲2号", 116.4471, 39.9282, 96, 21340, 9872, 47, "11:00-次日02:00", "很久以前"),
    ("大董烤鸭(团结湖店)", 1, "团结湖", "朝阳区团结湖北口3号楼", 116.4617, 39.9333, 320, 9820, 5610, 49, "11:00-21:30", "大董"),
    ("太二酸菜鱼(国贸店)", 1, "国贸", "朝阳区建国门外大街1号国贸商城B1", 116.4612, 39.9085, 88, 33120, 12045, 46, "10:30-22:00", "太二"),
    ("西贝莜面村(朝阳大悦城店)", 1, "朝阳大悦城", "朝阳区朝阳北路101号7层", 116.5223, 39.9243, 92, 27650, 8890, 45, "10:00-22:00", "西贝"),
    ("必胜客(蓝色港湾店)", 1, "蓝色港湾", "朝阳区朝阳公园路6号院1层", 116.4765, 39.9470, 105, 15620, 4321, 43, "10:00-22:00", "必胜客"),
    ("局气(望京店)", 1, "望京", "朝阳区望京街9号方恒购物中心5层", 116.4700, 39.9930, 110, 18900, 6720, 46, "10:30-21:30", "局气"),
    ("蔡澜港式点心(合生汇店)", 1, "合生汇", "朝阳区西大望路21号合生汇B2", 116.4869, 39.8942, 120, 22410, 7830, 47, "10:00-22:00", "蔡澜点心"),
    ("纯K(工体店)", 2, "工人体育场", "朝阳区工人体育场北路6号", 116.4478, 39.9298, 180, 8900, 3210, 45, "12:00-次日06:00", "纯K"),
    ("星聚会KTV(三里屯店)", 2, "三里屯", "朝阳区三里屯SOHO 5号商场4层", 116.4552, 39.9339, 150, 12040, 4350, 44, "12:00-次日05:00", "星聚会"),
    ("迷城密室逃脱(朝阳大悦城店)", 2, "朝阳大悦城", "朝阳区朝阳北路101号9层", 116.5220, 39.9248, 128, 6320, 2180, 46, "10:00-23:00", "迷城"),
    ("北京瑰丽酒店", 3, "国贸", "朝阳区呼家楼京广中心", 116.4585, 39.9237, 1680, 3200, 1890, 49, "24小时", "瑰丽"),
    ("全季酒店(国贸店)", 3, "国贸", "朝阳区建国路88号", 116.4598, 39.9092, 480, 15600, 6720, 45, "24小时", "全季"),
    ("克丽缇娜(三里屯店)", 4, "三里屯", "朝阳区三里屯北路11号院2层", 116.4548, 39.9341, 380, 4210, 1560, 46, "10:00-21:00", "克丽缇娜"),
    ("丝域养发(国贸店)", 4, "国贸", "朝阳区建国门外大街甲6号", 116.4604, 39.9087, 260, 6180, 2240, 45, "10:00-21:00", "丝域"),
    ("超级猩猩健身(三里屯店)", 5, "三里屯", "朝阳区三里屯SOHO 2号商场B1", 116.4556, 39.9330, 168, 9820, 3120, 47, "07:00-23:00", "超级猩猩"),
    ("乐刻运动(工体店)", 5, "工人体育场", "朝阳区工人体育场北路8号院", 116.4480, 39.9290, 99, 14200, 4890, 44, "24小时", "乐刻"),
    ("良子健身(朝阳门店)", 6, "朝阳门", "朝阳区朝阳门外大街18号", 116.4356, 39.9245, 168, 7310, 2450, 45, "10:00-次日02:00", "良子"),
    ("足下登高(望京店)", 6, "望京", "朝阳区望京南湖中园211号", 116.4705, 39.9925, 138, 5620, 1980, 44, "11:00-次日01:00", "足下登高"),
    ("meland club(朝阳大悦城店)", 7, "朝阳大悦城", "朝阳区朝阳北路101号6层", 116.5228, 39.9250, 268, 4210, 1670, 48, "09:30-21:00", "meland"),
    ("悠游堂(合生汇店)", 7, "合生汇", "朝阳区西大望路21号合生汇L3", 116.4872, 39.8938, 158, 5120, 1830, 45, "09:30-21:30", "悠游堂"),
    ("万达影城(CBD店)", 8, "国贸", "朝阳区建国路93号万达广场B座5层", 116.4601, 39.9080, 65, 32100, 11200, 46, "09:00-次日01:00", "万达影城"),
    ("英皇电影城(三里屯店)", 8, "三里屯", "朝阳区三里屯路19号院南区5层", 116.4549, 39.9335, 78, 18900, 6340, 47, "09:00-次日01:00", "英皇电影城"),
    ("尚品造型(三里屯店)", 9, "三里屯", "朝阳区三里屯SOHO 3号商场2层", 116.4553, 39.9343, 288, 3120, 1180, 46, "10:00-21:00", "尚品造型"),
    ("天堂超市(工体店)", 10, "工人体育场", "朝阳区工人体育场北路4号", 116.4474, 39.9285, 120, 16200, 5210, 44, "17:00-次日05:00", "天堂超市"),
    ("京A Brewpub(1949店)", 10, "三里屯", "朝阳区工体北路4号院1949会所", 116.4702, 39.9413, 150, 8900, 3240, 47, "11:00-次日01:00", "京A"),
    ("胡大饭馆(簋街总店)", 1, "簋街", "东城区东直门内大街233号", 116.4238, 39.9401, 130, 41200, 18900, 48, "10:00-次日04:00", "胡大"),
    ("南门涮肉(东直门店)", 1, "东直门", "东城区东直门内大街188号", 116.4356, 39.9412, 118, 26300, 9870, 47, "11:00-22:30", "南门涮肉"),
    ("星巴克臻选(北京坊店)", 1, "前门", "西城区廊房头条21号院", 116.3948, 39.8965, 45, 31200, 8900, 46, "07:00-22:00", "星巴克臻选"),
    ("喜茶(三里屯太古里店)", 1, "三里屯", "朝阳区三里屯路19号院南区1层", 116.4546, 39.9338, 28, 56200, 13400, 45, "10:00-22:00", "喜茶"),
    ("呷哺呷哺(国贸店)", 1, "国贸", "朝阳区建国门外大街1号国贸商城B1", 116.4607, 39.9090, 58, 22100, 6780, 43, "10:00-22:00", "呷哺呷哺"),
    ("云海肴(朝阳大悦城店)", 1, "朝阳大悦城", "朝阳区朝阳北路101号7层", 116.5225, 39.9245, 85, 13400, 4560, 45, "10:00-22:00", "云海肴"),
    ("Let's Relax 泰式按摩", 6, "三里屯", "朝阳区三里屯SOHO 5号商场3层", 116.4550, 39.9347, 268, 4210, 1620, 47, "11:00-24:00", "Let's Relax"),
    ("一兆韦德健身(国贸店)", 5, "国贸", "朝阳区建国路87号SKP-S 6层", 116.4610, 39.9095, 199, 6820, 2310, 45, "06:00-23:00", "一兆韦德"),
    ("樊登书店(三里屯店)", 2, "三里屯", "朝阳区三里屯SOHO 1号商场2层", 116.4555, 39.9332, 60, 3210, 1120, 46, "10:00-22:00", "樊登书店"),
    ("大玩家电玩城(合生汇店)", 2, "合生汇", "朝阳区西大望路21号合生汇L4", 116.4870, 39.8940, 88, 12400, 3980, 44, "10:00-22:00", "大玩家"),
    ("北京朝阳剧场", 2, "呼家楼", "朝阳区东三环北路36号", 116.4585, 39.9245, 120, 2100, 890, 45, "09:00-21:00", "朝阳剧场"),
    ("桔子水晶酒店(三里屯店)", 3, "三里屯", "朝阳区工体北路甲2号", 116.4558, 39.9336, 680, 8900, 3450, 46, "24小时", "桔子水晶"),
    ("木北造型(国贸店)", 9, "国贸", "朝阳区建国路88号SOHO现代城", 116.4609, 39.9089, 320, 5120, 1830, 47, "10:00-21:00", "木北造型"),
    ("小林家の日料(望京店)", 1, "望京", "朝阳区望京街10号院3层", 116.4708, 39.9935, 178, 7820, 2640, 47, "11:30-22:00", "小林家"),
]

# 店铺图片角标文案（第二张图的副标题）
SUBTITLES = ["招牌推荐", "人气必点", "环境实拍", "限时特惠", "深夜食堂", "小众宝藏"]


def svg_cover(shop_id, name, short, type_name, c1, c2, variant, subtitle):
    """生成一张 800x600 的抽象探店图（渐变 + 柔光斑 + 角标文字），适合做缩略图"""
    # 每张图的光斑布局略有不同
    layouts = [
        [(0.72, 0.22, 170, 0.42), (0.24, 0.78, 130, 0.30), (0.88, 0.70, 90, 0.22)],
        [(0.30, 0.28, 190, 0.40), (0.78, 0.62, 150, 0.32), (0.16, 0.82, 100, 0.20)],
        [(0.50, 0.16, 210, 0.36), (0.82, 0.84, 120, 0.26), (0.12, 0.54, 150, 0.30)],
    ][variant % 3]

    blobs = "".join(
        f'<circle cx="{int(800*cx)}" cy="{int(600*cy)}" r="{r}" fill="#ffffff" opacity="{op}" filter="url(#blur{shop_id}_{variant})"/>'
        for cx, cy, r, op in layouts
    )
    # 细线网格，增加质感
    lines = "".join(
        f'<line x1="0" y1="{i*120}" x2="800" y2="{i*120}" stroke="#ffffff" stroke-opacity="0.05" stroke-width="1"/>'
        for i in range(1, 5)
    )
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600">
  <defs>
    <linearGradient id="g{shop_id}_{variant}" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="{c1}"/>
      <stop offset="100%" stop-color="{c2}"/>
    </linearGradient>
    <filter id="blur{shop_id}_{variant}" x="-60%" y="-60%" width="220%" height="220%">
      <feGaussianBlur stdDeviation="58"/>
    </filter>
    <linearGradient id="v{shop_id}_{variant}" x1="0" y1="0" x2="0" y2="1">
      <stop offset="55%" stop-color="#000000" stop-opacity="0"/>
      <stop offset="100%" stop-color="#000000" stop-opacity="0.34"/>
    </linearGradient>
  </defs>
  <rect width="800" height="600" fill="url(#g{shop_id}_{variant})"/>
  {lines}
  {blobs}
  <rect width="800" height="600" fill="url(#v{shop_id}_{variant})"/>
</svg>'''


def svg_avatar(uid, label, c1, c2):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240">
  <defs><linearGradient id="a{uid}" x1="0" y1="0" x2="1" y2="1">
    <stop offset="0%" stop-color="{c1}"/><stop offset="100%" stop-color="{c2}"/>
  </linearGradient></defs>
  <rect width="240" height="240" fill="url(#a{uid})"/>
  <circle cx="196" cy="44" r="70" fill="#ffffff" opacity="0.10"/>
  <circle cx="36" cy="204" r="52" fill="#ffffff" opacity="0.08"/>
  <text x="120" y="120" text-anchor="middle" dominant-baseline="central"
        font-family="PingFang SC,Microsoft YaHei,sans-serif" font-size="96" font-weight="600"
        fill="#ffffff">{escape(label)}</text>
</svg>'''


type_name_map = {t[0]: t[1] for t in TYPES}

# ---- 生成店铺图 ----
for idx, s in enumerate(SHOPS, start=1):
    name, tid = s[0], s[1]
    short = s[11]
    c1, c2 = PALETTE[tid]
    for v in range(1, 3):
        sub = SUBTITLES[(idx + v) % len(SUBTITLES)] if v == 2 else "探店首选"
        path = os.path.join(IMG_SHOP, f"{idx}-{v}.svg")
        with open(path, "w", encoding="utf-8") as f:
            f.write(svg_cover(idx, name, short, type_name_map[tid], c1, c2, v, sub))

# ---- 生成头像 ----
AVATAR_COLORS = [
    ("#FF7A45", "#FFB020"), ("#6D5BF6", "#A78BFA"), ("#0EA5E9", "#38BDF8"),
    ("#EC4899", "#F9A8D4"), ("#059669", "#34D399"), ("#F59E0B", "#FCD34D"),
    ("#0D9488", "#2DD4BF"), ("#9333EA", "#C084FC"),
]
USERS = [
    (1, "13800000001", "罗耀", "罗"),
    (2, "13800000002", "甜品猎人小鹿", "鹿"),
    (3, "13800000003", "北京吃货老王", "王"),
    (4, "13800000004", "Momo在探店", "M"),
    (5, "13800000005", "深夜食堂阿汤", "汤"),
    (6, "13800000006", "柠檬不酸", "柠"),
    (7, "13800000007", "火锅永动机", "火"),
    (8, "13800000008", "周末去哪儿", "周"),
    (9, "13800000009", "一只吃货鹿", "吃"),
    (10, "13800000010", "CBD打工人", "C"),
    (11, "13800000011", "拿铁三分糖", "拿"),
    (12, "13800000012", "胡同串子", "胡"),
    (13, "13800000013", "小满", "满"),
    (14, "13800000014", "阿May", "A"),
    (15, "13800000015", "吃遍三里屯", "三"),
    (16, "13800000016", "健身阿汤", "健"),
    (17, "13800000017", "咖啡因依赖", "咖"),
    (18, "13800000018", "老张的日常", "张"),
    (19, "13800000019", "甜筒", "甜"),
    (20, "13800000020", "夜猫子", "夜"),
]
for uid, phone, nick, label in USERS:
    c1, c2 = AVATAR_COLORS[(uid - 1) % len(AVATAR_COLORS)]
    with open(os.path.join(IMG_AVATAR, f"{uid}.svg"), "w", encoding="utf-8") as f:
        f.write(svg_avatar(uid, label, c1, c2))


def esc(v):
    if isinstance(v, str):
        return "'" + v.replace("\\", "\\\\").replace("'", "''") + "'"
    return str(v)


lines = []
lines.append("USE hmdp;\n")
lines.append("SET NAMES utf8mb4;\n")

# ---------------- 分类 ----------------
lines.append("\n-- 商户分类")
vals = []
for tid, name, icon, sort in TYPES:
    vals.append(f"({tid}, {esc(name)}, {esc(icon)}, {sort})")
lines.append("INSERT INTO tb_shop_type (id, name, icon, sort) VALUES\n" + ",\n".join(vals) + ";\n")

# ---------------- 店铺 ----------------
lines.append("\n-- 商户")
vals = []
for idx, s in enumerate(SHOPS, start=1):
    (name, tid, area, address, x, y, avg, sold, comments, score, hours, short) = s
    images = f"/imgs/shop/{idx}-1.svg,/imgs/shop/{idx}-2.svg"
    vals.append(
        f"({idx}, {esc(name)}, {tid}, {esc(images)}, {esc(area)}, {esc(address)}, {x}, {y}, "
        f"{avg}, {sold}, {comments}, {score}, {esc(hours)})"
    )
lines.append(
    "INSERT INTO tb_shop (id, name, type_id, images, area, address, x, y, avg_price, sold, comments, score, open_hours) VALUES\n"
    + ",\n".join(vals) + ";\n"
)

# ---------------- 用户 ----------------
lines.append("\n-- 用户")
vals = []
for uid, phone, nick, label in USERS:
    vals.append(f"({uid}, {esc(phone)}, '', {esc(nick)}, {esc('/imgs/avatar/%d.svg' % uid)})")
lines.append("INSERT INTO tb_user (id, phone, password, nick_name, icon) VALUES\n" + ",\n".join(vals) + ";\n")

lines.append("\n-- 用户详情")
CITIES = ["北京", "北京", "北京", "上海", "北京"]
INTROS = [
    "干饭人，干饭魂", "甜品是另一个胃", "只推真心觉得好吃的店",
    "周末探店博主", "深夜营业中", "随便吃吃", "火锅终生会员",
    "打卡第 100 家店", "爱吃不胖体质", "国贸搬砖人", "咖啡续命",
]
vals = []
for uid, phone, nick, label in USERS:
    vals.append(
        f"({uid}, {esc(CITIES[(uid-1) % len(CITIES)])}, {esc(INTROS[(uid-1) % len(INTROS)])}, "
        f"{(uid % 2) + 1}, '1998-0{(uid % 9) + 1}-1{uid % 9}', {uid * 120}, {(uid % 6) + 1})"
    )
lines.append("INSERT INTO tb_user_info (user_id, city, introduce, gender, birthdays, credits, level) VALUES\n" + ",\n".join(vals) + ";\n")

# ---------------- 优惠券 ----------------
lines.append("\n-- 普通优惠券")
vals = []
vid = 1
normal_voucher_shops = []
for idx, s in enumerate(SHOPS, start=1):
    if idx in (9, 10, 11, 12, 13, 16, 17, 22, 23, 25, 26, 38):  # 这些店不发普通券，留给秒杀
        continue
    pay = 7000 + (idx % 5) * 1000
    actual = 8000 + (idx % 5) * 1000
    vals.append(
        f"({vid}, {idx}, {esc(f'{actual//100}元代金券')}, {esc('周一至周日可用')}, "
        f"{esc('仅限堂食，不与其他优惠同享；每桌限用1张。')}, {pay}, {actual}, 0, 1)"
    )
    normal_voucher_shops.append(idx)
    vid += 1
for v in vals:
    pass
# 分两批插入，避免语句过长
lines.append(
    "INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status) VALUES\n"
    + ",\n".join(vals) + ";\n"
)

# 秒杀券
lines.append("\n-- 秒杀券（库存有限，用于演示 Redis + Lua 原子扣减）")
seckill_shop_ids = [9, 10, 11, 12, 13, 16, 17, 22, 23, 25, 26, 38]
vals = []
svals = []
base_id = 101
for i, sid in enumerate(seckill_shop_ids):
    stock = 20 if i % 3 == 0 else 50
    vals.append(
        f"({base_id + i}, {sid}, {esc('100元代金券 · 限时秒杀')}, {esc('手慢无')}, "
        f"{esc('每人限购1张，秒杀成功后 7 天内有效。')}, 5000, 10000, 1, 1)"
    )
    svals.append(
        f"({base_id + i}, {stock}, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY))"
    )
lines.append(
    "INSERT INTO tb_voucher (id, shop_id, title, sub_title, rules, pay_value, actual_value, type, status) VALUES\n"
    + ",\n".join(vals) + ";\n"
)
lines.append(
    "\nINSERT INTO tb_seckill_voucher (voucher_id, stock, begin_time, end_time) VALUES\n"
    + ",\n".join(svals) + ";\n"
)

# ---------------- 探店笔记 ----------------
BLOG_TITLES = [
    "这家店的招牌菜真的会让人上头",
    "人均一百出头，吃到扶墙出",
    "在三里屯藏了家宝藏小店",
    "周末排队两小时，值不值？",
    "环境绝了，拍照超出片",
    "北京吃了一圈，还是这家最对味",
    "深夜十一点，这里还是满座",
    "带爸妈来吃，被夸了一整晚",
    "一个人也能好好吃饭",
    "朋友聚会首选，氛围感拉满",
    "第一次来就被圈粉了",
    "这家店我回购了不下十次",
    "打卡成功，下次还来",
    "藏在商场里的惊喜",
    "吃到一半才想起来拍照",
]
BLOG_CONTENT = [
    "位置很好找，进门就能闻到香味。招牌菜必点，火候刚刚好，调味也克制，吃完不会觉得腻。服务态度也在线，加水很及时。",
    "工作日中午来的，人不多。上菜速度快，分量足。性价比在这一片算是很高的了，适合跟同事一起来。",
    "环境是我喜欢的那种，灯光偏暖，座位间距够，聊天不会吵。菜品整体水平稳定，盲点基本不会踩雷。",
    "排了大概四十分钟，等位的时候有小食和茶水。上桌之后确实没让人失望，最推荐他们家的招牌，值得专门跑一趟。",
    "和朋友两个人点了一桌，最后全部清盘。价格比想象中友好，人均控制在一百出头。下次准备带家里人来。",
    "第一次来是朋友推荐的，吃完理解为什么评分这么高了。细节做得很到位，连餐具都很有质感。",
    "晚上十点多过来，居然还要等位。深夜食堂的氛围感很足，厨房是明档，能看到出餐全过程。",
    "整体体验超出预期。服务员会主动介绍菜品和吃法，对第一次来的客人很友好。停车也方便。",
]
lines.append("\n-- 探店笔记")
vals = []
for i in range(1, 31):
    shop = SHOPS[(i * 7) % len(SHOPS)][0]
    shop_id = (i * 7) % len(SHOPS) + 1
    uid = (i % 19) + 1
    title = BLOG_TITLES[(i - 1) % len(BLOG_TITLES)]
    content = BLOG_CONTENT[(i - 1) % len(BLOG_CONTENT)]
    images = f"/imgs/shop/{shop_id}-{(i % 2) + 1}.svg"
    vals.append(
        f"({i}, {shop_id}, {uid}, {esc(title)}, {esc(images)}, {esc(content)}, "
        f"{random.randint(12, 860)}, {random.randint(2, 96)}, DATE_SUB(NOW(), INTERVAL {i * 7} HOUR))"
    )
lines.append(
    "INSERT INTO tb_blog (id, shop_id, user_id, title, images, content, liked, comments, create_time) VALUES\n"
    + ",\n".join(vals) + ";\n"
)

# ---------------- 评论 ----------------
lines.append("\n-- 笔记评论")
COMMENTS = [
    "看着就好吃，周末冲！", "求地址～", "这家我也去过，确实不错",
    "人均多少呀？", "收藏了，谢谢分享", "图片拍得真好看",
    "上次去排了好久", "下次一起去啊",
]
vals = []
cid = 1
for b in range(1, 31):
    for k in range(2):
        uid = ((b + k) % 19) + 1
        vals.append(
            f"({cid}, {uid}, {b}, 0, 0, {esc(COMMENTS[(b + k) % len(COMMENTS)])}, {random.randint(0, 30)}, 0, "
            f"DATE_SUB(NOW(), INTERVAL {b * 3} HOUR))"
        )
        cid += 1
lines.append(
    "INSERT INTO tb_blog_comments (id, user_id, blog_id, parent_id, answer_id, content, liked, status, create_time) VALUES\n"
    + ",\n".join(vals) + ";\n"
)

# ---------------- 关注关系 ----------------
lines.append("\n-- 关注关系（用户1关注2~11）")
vals = []
fid = 1
for target in range(2, 12):
    vals.append(f"({fid}, 1, {target})")
    fid += 1
for src in [2, 3, 4, 5, 6]:
    for target in [1, 7, 8, 9]:
        if src != target:
            vals.append(f"({fid}, {src}, {target})")
            fid += 1
lines.append("INSERT INTO tb_follow (id, user_id, follow_user_id) VALUES\n" + ",\n".join(vals) + ";\n")

with open(os.path.join(SQL_DIR, "data.sql"), "w", encoding="utf-8") as f:
    f.write("\n".join(lines))

print("images:", len(os.listdir(IMG_SHOP)), "shops,", len(os.listdir(IMG_AVATAR)), "avatars")
print("data.sql lines:", len("\n".join(lines).splitlines()))
