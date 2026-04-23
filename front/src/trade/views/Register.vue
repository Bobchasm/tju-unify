<template>
  <div class="back-btn-container">
    <BackButton />
  </div>
  <div class="wrapper">
    <header>
      <p>用户注册</p>
    </header>

    <ul class="form-box">
      <li class="form-item avatar-item">
        <label for="avatarFile" class="form-item-title">用户头像：</label>
        <div class="form-item-content">
          <div id="image-upload-area" class="image-upload-area" @click="triggerFileInput">
            <span v-if="!avatar" class="upload-icon">+</span>
            <span v-if="!avatar" class="upload-text">上传头像</span>
            <img v-if="avatar" :src="avatar" class="image-preview" alt="用户头像预览" />
            <input id="avatarFile" type="file" accept="image/*" class="file-input" @change="handleFileChange" ref="fileInput" />
          </div>
        </div>
      </li>
      <li class="form-item">
        <label for="username" class="form-item-title">用户名称：</label>
        <div class="form-item-content">
          <input
            id="username"
            type="text"
            v-model="user.username"
            placeholder="用户名称"
            maxlength="20"
          />
          <p class="word-count">{{ user.username.length }}/20</p>
        </div>
      </li>
      <li class="form-item">
        <label for="firstname" class="form-item-title">姓：</label>
        <div class="form-item-content">
          <input
            id="firstname"
            type="text"
            v-model="user.firstname"
            placeholder="姓"
          />
        </div>
      </li>
      <li class="form-item">
        <label for="lastname" class="form-item-title">名：</label>
        <div class="form-item-content">
          <input
            id="lastname"
            type="text"
            v-model="user.lastname"
            placeholder="名"
          />
        </div>
      </li>
      <li class="form-item">
        <label for="phone" class="form-item-title">手机号码：</label>
        <div class="form-item-content">
          <input id="phone" type="text" v-model="user.phone" placeholder="手机号码" />
        </div>
      </li>
      <li class="form-item">
        <label for="password" class="form-item-title">密码：</label>
        <div class="form-item-content">
          <input id="password" type="password" v-model="user.password" placeholder="密码" />
        </div>
      </li>
      <li class="form-item">
        <label for="confirmPassword" class="form-item-title">确认密码：</label>
        <div class="form-item-content">
          <input id="confirmPassword" type="password" v-model="confirmPassword" placeholder="确认密码" />
        </div>
      </li>
      <li class="form-item">
        <label for="useremail" class="form-item-title">邮箱：</label>
        <div class="form-item-content">
          <input id="useremail" type="text" v-model="user.useremail" placeholder="请输入邮箱" />
        </div>
      </li>
      <li class="form-item gender-item">
        <div class="form-item-title">性别：</div>
        <div class="form-item-content">
          <input type="radio" v-model="user.usersex" value="1" id="male" />
          <label for="male">男</label>
          <input type="radio" v-model="user.usersex" value="0" id="female" />
          <label for="female">女</label>
        </div>
      </li>
    </ul>

    <div class="button-register">
      <button @click="register">注册</button>
    </div>

    <div v-if="messageBoxVisible" class="message-box-overlay">
      <div class="message-box">
        <p>{{ messageBoxMessage }}</p>
        <button @click="closeMessageBox">确定</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/trade/utils/tradeRequest';
import { toast } from '@/trade/utils/toast';
import BackButton from '../components/BackButton.vue';

export default {
  name: 'Register',
  components:{
    BackButton
  },
  setup() {
    const router = useRouter();
    const user = reactive({
      phone: '',
      password: '',
      username: '',
      useremail: '',
      usersex: 0,
      firstname: '',
      lastname: '',
      photo: ''
    });
    const confirmPassword = ref('');
    const avatar = ref(null);
    const uploadedFile = ref(null);

    const messageBoxVisible = ref(false);
    const messageBoxMessage = ref('');

    const showMessageBox = (message) => {
      messageBoxMessage.value = message;
      messageBoxVisible.value = true;
    };

    const closeMessageBox = () => {
      messageBoxVisible.value = false;
    };

    const triggerFileInput = () => {
      document.getElementById('avatarFile').click();
    };

const handleFileChange = (event) => {
  const file = event.target.files[0];
  
  if (file) {
    uploadedFile.value = file;
    const reader = new FileReader();
    
    reader.onload = (e) => {
      avatar.value = e.target.result;
    };
    
    try {
      reader.readAsDataURL(file);
    } catch (error) {
      console.error('头像读取出错:', error.message || '文件读取失败');
      toast.error("头像预览失败，请重试");
    }
  } else {
    avatar.value = null;
    uploadedFile.value = null;
  }
};


    const register = async () => {
      user.username = user.username.trim();
      if (!user.username) {
        showMessageBox('用户名不能为空！');
        return;
      }
      if (user.username.length > 20) {
        showMessageBox('用户名过长，请勿超过20个字符！');
        return;
      }

      if (!user.firstname) {
        showMessageBox('姓不能为空！');
        return;
      }
      if (!user.lastname) {
        showMessageBox('名不能为空！');
        return;
      }

      const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;
      if (!user.password || !passwordRegex.test(user.password)) {
        showMessageBox('密码格式错误，请确保包含至少一个大写字母、一个小写字母和一个数字，长度至少为8个字符。');
        return;
      }
      if (user.password !== confirmPassword.value) {
        showMessageBox('两次输入的密码不一致！');
        return;
      }
      
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!user.useremail || !emailRegex.test(user.useremail)) {
        showMessageBox('邮箱格式不正确！');
        return;
      }

      if (!uploadedFile.value) {
        showMessageBox('请先上传头像！');
        return;
      }

      try {
        const formData = new FormData();
        formData.append('file', uploadedFile.value);
        const uploadResponse = await request.post('/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (uploadResponse && uploadResponse.success && uploadResponse.data) {
          user.photo = uploadResponse.data;
          console.log('头像上传成功，URL已保存:', user.photo);
        } else {
          showMessageBox(uploadResponse?.message || '图片上传失败，请重试');
          return;
        }
      } catch (error) {
        console.error('头像上传出错:', error.response?.data?.message || error.message);
        showMessageBox('头像上传出错，请检查网络！');
        return;
      }

      const registerPayload = {
        username: user.username,
        password: user.password,
        phone: user.phone,
        email: user.useremail,
        firstName: user.firstname,
        lastName: user.lastname,
        photo: user.photo,
        gender: user.usersex
      };
      
      try {
        const response = await request.post('/api/register', registerPayload);
        console.log('注册请求成功，服务器响应数据:', response);

        if (response && response.success) {
          showMessageBox('注册成功！');
          setTimeout(() => {
            router.push({ path: '/trade' });
          }, 1500);
        } else {
          let errorMessage = '注册失败！服务器返回了无效数据。';
          if (response && response.message) {
            errorMessage = `注册失败！原因：${response.message}`;
          }
          showMessageBox(errorMessage);
        }
      } catch (error) {
        console.error('注册请求发生错误:', error.response || error.message);
        if (error.response?.status === 409) {
          showMessageBox('此用户名已存在！');
        } else {
          showMessageBox('请求失败，请检查网络或服务器！');
        }
      }
    };

    return {
      user,
      confirmPassword,
      avatar,
      uploadedFile,
      triggerFileInput,
      handleFileChange,
      register,
      messageBoxVisible,
      messageBoxMessage,
      closeMessageBox
    };
  }
};
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
  padding-bottom: 20px;
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

.form-item {
  display: flex;
  align-items: center; /* 确保所有表单项内容垂直居中 */
  margin-bottom: 20px;
}

.form-item-title {
  font-size: 16px;
  font-weight: 600;
  color: #444;
  flex-basis: 90px;
  flex-shrink: 0;
  padding-right: 10px; /* 标题与内容间距 */
}

.form-item-content {
  flex: 1;
}

.form-item-content input[type='text'],
.form-item-content input[type='password'] {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px; /* 8px圆角 */
  font-size: 16px;
  color: #333;
  transition: border-color 0.3s;
}

.form-item-content input[type='text']:focus,
.form-item-content input[type='password']:focus {
  outline: none;
  border-color: #0097FF;
}

.form-box .gender-item {
  margin-top: 20px; /* 在性别选项上方增加外边距 */
  margin-bottom: 20px; /* 保持与下方按钮的距离 */
}

.form-item.gender-item .form-item-content {
  display: flex;
  align-items: center;
}

.form-item.gender-item .form-item-content input[type='radio'] {
  width: 16px;
  height: 16px;
  margin-right: 6px;
  vertical-align: middle;
  cursor: pointer;
}

.form-item.gender-item .form-item-content label {
  font-size: 14px;
  color: #666;
  margin-right: 15px;
  cursor: pointer;
}

.image-upload-area {
  position: relative;
  width: 80px;
  height: 80px;
  border: 2px dashed #ccc;
  border-radius: 50%; /* 圆形 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: border-color 0.3s, background-color 0.3s;
  overflow: hidden;
}

.image-upload-area:hover {
  border-color: #0097ff;
  background-color: #f9f9f9;
}

.upload-icon {
  font-size: 30px;
  color: #999;
}

.upload-text {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.image-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.file-input {
  display: none; /* 隐藏原始文件输入框 */
}

.word-count {
  font-size: 12px;
  color: #999;
  text-align: right;
  margin-top: 4px;
}

.button-register {
  width: 90%;
  max-width: 400px;
  margin-top: 20px;
}

.button-register button {
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

.button-register button:hover {
  background-color: #0097ff;
}

.button-register button:active {
  transform: translateY(1px);
}

.message-box-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.message-box {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 80%;
  max-width: 350px;
}

.message-box p {
  margin-bottom: 20px;
  font-size: 16px;
  color: #333;
}

.message-box button {
  background: #0097ff;
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}
.back-btn-container {
  position: fixed; /* 固定定位，不随滚动移动 */
  left: 0vw; /* 距离左侧的距离，可根据需求调整 */
  top: 2vw; /* 距离顶部的距离，与 header 高度（12vw）适配，确保垂直居中 */
  z-index: 1001; /* 比 header 的 z-index:1000 高，避免被遮挡 */
}
</style>