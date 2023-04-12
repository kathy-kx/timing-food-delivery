/* 自定义trim */
function trim (str) {  //删除左右两端的空格,自定义的trim()方法
  return str == undefined ? "" : str.replace(/(^\s*)|(\s*$)/g, "")
}

//获取url地址上面的参数。例如requestUrlParam('id') 传进来argname='id'。这个方法就是要获得id的值
function requestUrlParam(argname){
  var url = location.href
    //alert(url)  //http://localhost:8080/backend/page/member/add.html?id=1626479515129643009 含有我们需要的id值
  var arrStr = url.substring(url.indexOf("?")+1).split("&") //将url中 ?后面的字符串按照&分割，成为字符串数组
  for(var i =0;i<arrStr.length;i++) //遍历数组
  {
      var loc = arrStr[i].indexOf(argname+"=")//找到"id="这一字符串 在arrStr[i]的起始index
      if(loc!=-1){  //能找到"id="，就把"id="替换成空字符串，也就提取出了id的值
          return arrStr[i].replace(argname+"=","").replace("?","")
      }
  }
  return ""
}
