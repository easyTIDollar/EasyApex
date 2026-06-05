with open(r'F:\EasyApex\app\src\main\java\com\easyapex\ApexStatsScreen.kt', 'r', encoding='utf-8') as f:
    lines = f.readlines()

def_idx = 77
sec_idx = 239

# Fix the call
lines[73] = '                    SettingsDialog(currentTheme = currentTheme, onThemeChange = { newTheme -> viewModel.setTheme(newTheme) }, onDismiss = { showSettingsDialog = false }, viewModel = viewModel)\n'

print('Before def:', def_idx, 'From sec:', sec_idx, 'Total:', len(lines))

# Keep lines before def, skip old dialog, keep from section
result = lines[:def_idx] + lines[sec_idx:]
print('After merge:', len(result))

with open(r'F:\EasyApex\app\src\main\java\com\easyapex\ApexStatsScreen.kt', 'w', encoding='utf-8') as f:
    f.writelines(result)
print('Saved')