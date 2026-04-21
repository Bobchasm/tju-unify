<template>
  <div class="search-test">
    <h2>搜索功能测试</h2>
    
    <div class="test-form">
      <div class="form-group">
        <label>搜索关键词:</label>
        <input v-model="testKeyword" type="text" placeholder="输入搜索关键词" />
      </div>
      
      <div class="form-group">
        <label>排序方式:</label>
        <select v-model="testSortBy">
          <option value="default">综合排序</option>
          <option value="score">按评分排序</option>
          <option value="sales">按销量排序</option>
        </select>
      </div>
      
      <button @click="testSearch" :disabled="loading">测试搜索</button>
    </div>
    
    <div v-if="loading" class="loading">
      <p>搜索中...</p>
    </div>
    
    <div v-if="searchResult" class="result">
      <h3>搜索结果:</h3>
      <pre>{{ JSON.stringify(searchResult, null, 2) }}</pre>
    </div>
    
    <div v-if="error" class="error">
      <h3>错误信息:</h3>
      <p>{{ error }}</p>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import request from '@/trade/utils/tradeRequest';

export default {
  name: 'SearchTest',
  setup() {
    const testKeyword = ref('美食');
    const testSortBy = ref('default');
    const searchResult = ref(null);
    const error = ref(null);
    const loading = ref(false);
    
    const testSearch = async () => {
      loading.value = true;
      error.value = null;
      searchResult.value = null;
      
      try {
        const params = {
          keyword: testKeyword.value
        };
        
        if (testSortBy.value === 'score') {
          params.isScore = 1;
          params.isSales = 0;
        } else if (testSortBy.value === 'sales') {
          params.isScore = 0;
          params.isSales = 1;
        } else {
          params.isScore = 0;
          params.isSales = 0;
        }
        
        console.log('测试参数:', params);
        
        const response = await request.get('/api/businesses/search', { params });
        searchResult.value = response;
        
      } catch (err) {
        error.value = err.message || '搜索失败';
        console.error('搜索错误:', err);
      } finally {
        loading.value = false;
      }
    };
    
    return {
      testKeyword,
      testSortBy,
      searchResult,
      error,
      loading,
      testSearch
    };
  }
};
</script>

<style scoped>
.search-test {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.test-form {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  background: #0097ff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.result {
  background: #f0f8ff;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.result pre {
  background: white;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
}

.error {
  background: #ffe6e6;
  padding: 15px;
  border-radius: 4px;
  color: #d00;
}
</style>
