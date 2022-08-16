// 示例 input = 1,2
var platArr = input.split(',');

var result = false;
for (var i = 0; i < platArr.length; i++) {
    if (platform == platArr[i].trim()) {
        result = true;
        break;
    }
}

// 输出result
result;