const fs = require('fs');
const path = require('path');

// 复制 vnollx.jpg 从 src/assets 到 public
const sourcePath = path.join(__dirname, 'src', 'assets', 'vnollx.jpg');
const destPath = path.join(__dirname, 'public', 'vnollx.jpg');

try {
  fs.copyFileSync(sourcePath, destPath);
  console.log('✅ vnollx.jpg 已复制到 public 目录');
} catch (error) {
  console.log('❌ 复制失败:', error.message);
}
