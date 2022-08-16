// 示例 input = 2.5.6

// 用户请求的版本
var reqVersion = version.split('.');
// 配置的版本
var inputVersion = input.split('.');

for (var i = 0; i < 3; i++) {
    // 格式化为int
    reqVersion[i] = parseInt(reqVersion[i]);
    inputVersion[i] = parseInt(inputVersion[i]);
}

var result = true;
for (var i = 0; i < 3; i++) {
    // print(reqVersion[i]); // print for debug

    if (reqVersion[i] > inputVersion[i]) {
        // 直接结束
        break;
    } else if (reqVersion[i] < inputVersion[i]) {
        result = false;
        break;
    } else { // 二者相等，不作处理
        // 继续
    }
}

// 输出result
result;