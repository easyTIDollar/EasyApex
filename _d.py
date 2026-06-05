with open(r'F:\EasyApex\app\src\main\java\com\easyapex\ApexStatsScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    s = line.strip()
    if s == 'fun SettingsDialog(':
        print('DEF at ' + str(i))
    if '1. 玩家查询页面' in s:
        print('SEC at ' + str(i))
    if 'SettingsDialog(' in s and 'currentTheme' in s:
        print('CALL at ' + str(i) + ': ' + s[:100])