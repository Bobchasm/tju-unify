<template>
  <div class="menu-section address-section">
    <div class="section-title">我的收货地址</div>
    <div class="menu-list">
      <div v-if="addresses.length === 0 && !isLoading" class="empty-state">
        暂无收货地址，请添加。
      </div>
      <div v-if="isLoading" class="loading-state">
        <i class="fas fa-spinner fa-spin"></i> 正在加载...
      </div>
      <div v-for="address in addresses" :key="address.id" class="address-item">
        <div class="address-details">
          <span class="contact-info">{{ address.contactName }} ({{ address.contactTel }})</span>
          <span class="full-address">{{ address.address }}</span>
        </div>
        <div class="address-actions">
          <i class="fas fa-edit edit-icon" @click="openAddressModal(address)"></i>
          <i class="fas fa-trash-alt delete-icon" @click="showDeleteConfirm(address)"></i>
        </div>
      </div>
      <div class="add-new-item" @click="openAddressModal()">
        <i class="fas fa-plus-circle add-icon"></i>
        <span class="add-text">新增收货地址</span>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showAddressModal" class="modal-overlay" @click.self="closeAddressModal">
        <div class="modal-content">
          <h3>{{ isEditing ? '编辑地址' : '新增地址' }}</h3>
          <div class="modal-item">
            <label>收货人</label>
            <input v-model="addressForm.contactName" placeholder="输入收货人姓名" />
          </div>
          <div class="modal-item">
            <label>手机号</label>
            <input 
              v-model="addressForm.contactTel" 
              placeholder="输入手机号" 
              :class="{ 'input-error': addressForm.contactTel && !validatePhoneNumber(addressForm.contactTel) }"
            />
            <div v-if="addressForm.contactTel && !validatePhoneNumber(addressForm.contactTel)" class="error-message">
              请输入正确的手机号码格式（11位数字，以1开头）
            </div>
          </div>
          <div class="modal-item">
            <div class="title">
			        性别：
              </div>
              <div class="content" style="font-size: 4vw;">
                <input type="radio" v-model="addressForm.contactSex" value="1" style="width:6vw;height:3.2vw;">男
                <input type="radio" v-model="addressForm.contactSex" value="0" style="width:6vw;height:3.2vw;">女
              </div>
            </div>
          <div class="modal-item">
            <label>详细地址</label>
            <textarea v-model="addressForm.address" placeholder="输入详细地址"></textarea>
          </div>

          <div class="modal-buttons">
            <button @click="submitAddress">提交</button>
            <button @click="closeAddressModal">取消</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 删除确认弹窗 -->
    <Teleport to="body">
      <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
        <div class="modal-content">
          <div class="modal-header">
            <h3>确认删除</h3>
            <span class="close-btn" @click="closeDeleteModal">&times;</span>
          </div>
          <div class="modal-body">
            <p>确定要删除收货地址 "{{ selectedAddress?.contactName }}" 吗？</p>
          </div>
          <div class="modal-footer">
            <button class="modal-btn confirm-btn" @click="confirmDelete">确认</button>
            <button class="modal-btn cancel-btn" @click="closeDeleteModal">取消</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { toast } from '@/trade/utils/toast';
import request from '@/trade/utils/tradeRequest';

const props = defineProps({
  id: String
});

const addresses = ref([]);
const isLoading = ref(false);
const showAddressModal = ref(false);
const isEditing = ref(false);
const showDeleteModal = ref(false);
const selectedAddress = ref(null);
const addressForm = ref({
  id: null,
  contactName: '',
  contactTel: '',
  contactSex: 0,
  address: '',
  userId: props.id
});
const userFromLocal = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null;
const userFromSession = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
const user = userFromLocal || userFromSession;

const validatePhoneNumber = (phone) => {
  const phoneRegex = /^1[3-9]\d{9}$/;
  return phoneRegex.test(phone);
};

onMounted(() => {
  loadAddresses();
});

const loadAddresses = async () => {
  if (!props.id) {
    toast.warning('用户未登录，无法获取地址列表');
    return;
  }

  isLoading.value = true;
  try {
    const response = await request.get('/api/addresses/listDeliveryAddressByUserId', { params: { userId: props.id} });
    addresses.value = response.data;
  } catch (error) {
    console.error('获取地址列表失败:', error);
    toast.error('获取地址列表失败，请重试！');
  } finally {
    isLoading.value = false;
  }
};

const openAddressModal = (address = null) => {
  document.body.style.overflow = 'hidden';

  if (address) {
    isEditing.value = true;
    addressForm.value = { ...address };
  } else {
    isEditing.value = false;
    addressForm.value = {
      id: null,
      contactName: '',
      contactTel: '',
      contactSex: null,
      address: '',
      userId: props.id
    };
  }
  showAddressModal.value = true;
};

const closeAddressModal = () => {
  document.body.style.overflow = '';
  showAddressModal.value = false;
};

const closeDeleteModal = () => {
  document.body.style.overflow = '';
  showDeleteModal.value = false;
  selectedAddress.value = null;
};

const submitAddress = async () => {
  const form = addressForm.value;
  
  if (!form.contactName || !form.contactTel || !form.address) {
    toast.warning('请填写完整的地址信息！');
    return;
  }

  if (!validatePhoneNumber(form.contactTel)) {
    toast.warning('请输入正确的手机号码格式（11位数字，以1开头）！');
    return;
  }

  try {
    if (isEditing.value) {
      await request.post('/api/addresses/updateDeliveryAddress', { ...form });
      toast.success('地址修改成功！');
    } else {
      await request.post('/api/addresses', {
        contactName: form.contactName,
        contactTel: form.contactTel,
        contactSex: form.contactSex,
        address: form.address,
        customer: { username: user.username }
      });
      toast.success('地址添加成功！');
    }
    closeAddressModal();
    loadAddresses();
  } catch (error) {
    console.error('操作失败:', error);
    if (error.response && error.response.data && error.response.data.message) {
      toast.error(`操作失败：${error.response.data.message}`);
    } else {
      toast.error('操作失败，请重试！');
    }
  }
};

const showDeleteConfirm = (address) => {
  selectedAddress.value = address;
  showDeleteModal.value = true;
  document.body.style.overflow = 'hidden';
};

const confirmDelete = async () => {
  if (!selectedAddress.value) return;

  try {
    await request.put('/api/addresses/removeDeliveryAddress', { id: selectedAddress.value.id });
    toast.success('地址删除成功！');
    closeDeleteModal();
    loadAddresses();
  } catch (error) {
    console.error('删除失败:', error);
    if (error.response && error.response.data && error.response.data.message) {
      toast.error(`删除失败：${error.response.data.message}`);
    } else {
      toast.error('删除失败，请重试！');
    }
  }
};
</script>

<style scoped>
.menu-section {
  width: 92%;
  max-width: 500px;
  margin: 20px auto;
  transform: translateY(-50px);
}
.section-title {
  font-size: 1.1rem;
  color: #2c3e50;
  margin-top: 30px;
  margin-bottom:10px;
  padding-left: 10px;
  font-weight: 600;
  border-left: 4px solid #3498db;
}
.menu-list {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
}
.address-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}
.address-item:hover {
  background-color: #f1f8ff;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.08);
}
.address-item:last-child {
  border-bottom: none;
}
.address-details {
  display: flex;
  flex-direction: column;
  flex: 1;
}
.contact-info {
  font-size: 1rem;
  font-weight: 600;
  color: #34495e;
}
.full-address {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-top: 5px;
}
.address-actions {
  display: flex;
  gap: 15px;
  margin-left: 20px;
}
.address-actions .edit-icon, .address-actions .delete-icon {
  color: #3498db;
  font-size: 1.1rem;
}
.address-actions .delete-icon {
  color: #e74c3c;
}
.add-new-item {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 20px;
  cursor: pointer;
  background: #f8f9fa;
  border-bottom-left-radius: 16px;
  border-bottom-right-radius: 16px;
  transition: background-color 0.3s ease;
}
.add-new-item:hover {
  background-color: #e9ecef;
}
.add-icon {
  color: #3498db;
  margin-right: 10px;
  font-size: 1.2rem;
}
.add-text {
  font-size: 1rem;
  color: #3498db;
}
.empty-state, .loading-state {
  text-align: center;
  padding: 20px;
  color: #7f8c8d;
  font-size: 0.9rem;
}
.loading-state .fa-spinner {
  margin-right: 5px;
}
</style>

<style>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
  backdrop-filter: blur(3px);
}

.modal-content {
  background: white;
  padding: 25px;
  border-radius: 16px;
  max-width: 420px;
  width: 85%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

.modal-overlay .modal-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  gap: 15px;
  animation: fadeIn 0.3s ease-out;
  max-height: none;
  overflow-y: visible;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95) translateY(20px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.modal-content h3 {
  margin-top: 0;
  color: #2c3e50;
  margin-bottom: 20px;
  font-size: 1.3rem;
}
.modal-item {
  margin-bottom: 15px;
  text-align: left;
}
.modal-item label {
  display: block;
  font-weight: 500;
  color: #555;
  margin-bottom: 5px;
  font-size: 0.95rem;
}
.modal-content input, .modal-content textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: border-color 0.3s;
}
.modal-content input:focus, .modal-content textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}
.input-error {
  border-color: #e74c3c !important;
  box-shadow: 0 0 0 2px rgba(231, 76, 60, 0.2) !important;
}
.error-message {
  color: #e74c3c;
  font-size: 0.85rem;
  margin-top: 5px;
  display: block;
}
.modal-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
.modal-buttons button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s;
}
.modal-buttons button:first-child {
  background: #3498db;
  color: white;
}
.modal-buttons button:first-child:hover {
  background: #2980b9;
  transform: translateY(-1px);
}
.modal-buttons button:last-child {
  background: #e0e0e0;
  color: #333;
}
.modal-buttons button:last-child:hover {
  background: #c7c7c7;
  transform: translateY(-1px);
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}
.radio-group {
  display: flex;
  gap: 15px; /* 两个选项之间的间距 */
  margin-top: 5px;
}

.radio-group input {
  margin-right: 5px; /* 单选按钮和文字的间距 */
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  color: #333;
}

.close-btn {
  font-size: 1.5rem;
  color: #aaa;
  cursor: pointer;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #666;
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.modal-body p {
  color: #555;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.modal-btn {
  border: none;
  border-radius: 20px;
  padding: 10px 20px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-btn {
  background-color: #e0e0e0;
  color: #333;
}

.cancel-btn:hover {
  background-color: #c7c7c7;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.confirm-btn {
  background-color: #1e80ff;
  color: white;
}

.confirm-btn:hover {
  background-color: #0085e0;
  box-shadow: 0 4px 12px rgba(30, 128, 255, 0.3);
}
</style>