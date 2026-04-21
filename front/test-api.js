
const axios = require('axios');

async function test() {
  try {
    console.log('正在测试登录接口...');
    
    const response = await axios.post('http://localhost:7070/api/auth', {
      username: 'admin',
      password: 'Admin123',
      rememberMe: true
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    console.log('状态码:', response.status);
    console.log('响应头:', response.headers);
    console.log('响应体:', response.data);
    
    if (response.data && response.data.id_token) {
      console.log('✅ 成功获取 token:', response.data.id_token);
    }
    
  } catch (error) {
    console.error('❌ 错误:', error.message);
    if (error.response) {
      console.error('响应状态:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
  }
}

test();
