function loginApi(data) {
  return $axios({
    // 向/employee/login发送post请求，并且带上data数据（json用户名密码）
    'url': '/employee/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/employee/logout',
    'method': 'post',
  })
}
