<template>
  <div class="back-btn-container">
    <BackButton />
  </div>
  <div class="wrapper">
    <!-- header部分 -->
    <header>
      <p>用户登陆</p>
    </header>

    <!-- 表单部分 -->
    <ul class="form-box">
      <li>
        <div class="title">
          用户名：
        </div>
        <div class="content">
          <input type="text" v-model="userName" placeholder="用户名">
        </div>
      </li>
      <li>
        <div class="title">
          密码：
        </div>
        <div class="content">
          <input type="password" v-model="password" placeholder="密码">
        </div>
      </li>
      <li style="justify-content: flex-end; padding-top: 2vw;">
        <label style="display: flex; align-items: center; font-size: 3vw; color: #666;">
          <input type="checkbox" v-model="rememberMe" style="width: 3vw; height: 3vw; margin-right: 1vw;">
          记住我
        </label>
      </li>
    </ul>

    <div class="button-login">
      <button @click="login">用户登录</button>
    </div>

    <!-- 底部菜单部分 -->

  </div>
</template>
  
<script>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import Footer from '../components/Footer.vue';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';

export default {
  name: 'Login',
  setup() {
    const userName = ref('');
    const password = ref('');
    const router = useRouter();
    const rememberMe = ref(false);
    const savedUserName = computed(() => {
      return localStorage.getItem('savedUserName') || '';
    });

    const login = async () => {
      if (!userName.value.trim()) {
        toast.error("用户名不能为空！");
        return;
      }
      if (!password.value.trim()) {
        toast.error("密码不能为空！");
        return;
      }

      try {
        const res = await request.post('/api/auth', {
          username: userName.value.trim(),
          password: password.value.trim(),
          rememberMe: rememberMe.value
        });

        if (!res) {
          toast.error(res.message);
          return;
        }

        const idToken = res?.id_token;
        console.log(idToken);
        if (!idToken) {
          toast.error(res.message);
          return;
        }

        const storage = rememberMe.value ? localStorage : sessionStorage;
        storage.setItem('token', idToken); // 存储 token（key 为 token）
        console.log(storage.getItem('token'));
        let userRes;

        try {
          userRes = await request.get('/api/user');
          if (userRes) {
            storage.setItem('userInfo', JSON.stringify(userRes));
          }
        } catch (error) {
          console.error('获取用户信息失败:', error);
        }
        console.log(storage.getItem('userInfo'));


        if (rememberMe.value) {
          localStorage.setItem('savedUserName', userName.value.trim());
        } else {
          localStorage.removeItem('savedUserName'); // 未勾选则清除
        }
        let targetPath = '/trade'; // 顾客端首页（交易平台独立后端）
        console.log(userRes.authorities);
        if (userRes?.authorities && Array.isArray(userRes.authorities)) {
          console.log(userRes.authorities);
          const isAdmin = userRes.authorities.some(auth => auth.name === 'ADMIN');
          if (isAdmin) {
            targetPath = '/trade';
          }
        }
        router.push({ path: targetPath });

      } catch (error) {
        const errorMsg = error.response?.data?.message || '网络异常，登录失败！';
        toast.error(errorMsg);
        console.error('登录错误:', error);
      }
    };


    return {
      userName,
      password,
      login,
      rememberMe,
      savedUserName
    };
  },
  components: {
    Footer,
    BackButton
  }
}
</script>
  
<style scoped>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Arial', sans-serif;
  background-color: #f0f2f5;
}

.wrapper {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #f0f2f5;
}

header {
  width: 100%;
  height: 15vw;
  max-height: 80px;
  background-color: #0097FF;
  color: #fff;
  font-size: clamp(20px, 5vw, 24px);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.form-box {
  width: 90%;
  max-width: 400px;
  background: #fff;
  margin-top: 25vw;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  list-style: none;
}

.form-box li {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.form-box li .title {
  font-size: 16px;
  font-weight: 600;
  color: #444;
  flex-basis: 90px;
  flex-shrink: 0;
}

.form-box li .content {
  flex: 1;
}

.form-box li .content input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  color: #333;
  transition: border-color 0.3s;
}

.form-box li .content input:focus {
  outline: none;
  border-color: #0097FF;
}

.form-box li:last-child {
  margin-bottom: 0;
  padding-top: 10px;
}

.form-box li label {
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.form-box li input[type="checkbox"] {
  width: 16px !important;
  height: 16px !important;
  margin-right: 6px;
  accent-color: #0097FF;
}

.button-login {
  width: 90%;
  max-width: 400px;
  margin-top: 20px;
}

.button-login button {
  width: 100%;
  height: 50px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  background-color: #0097FF;
  border-radius: 8px;
  border: none;
  outline: none;
  cursor: pointer;
  transition: background-color 0.3s, transform 0.1s, box-shadow 0.3s;
  box-shadow: 0 4px 12px rgba(0, 151, 255, 0.3);
}

.button-login button:hover {
  background-color: #007acc;
}

.button-login button:active {
  transform: translateY(1px);
}

.wrapper .button-register {
  width: 100%;
  box-sizing: border-box;
  padding: 4vw 3vw 0 3vw;
}

.wrapper .button-register button {
  width: 100%;
  height: 10vw;
  font-size: 3.8vw;
  font-weight: 700;
  color: #666;
  background-color: #EEE;
  border: solid 1px #DDD;
  border-radius: 4px;
  border: none;
  outline: none;
}

.back-btn-container {
  position: fixed;
  left: 0vw;
  top: 2vw;
  z-index: 1001;
}</style>