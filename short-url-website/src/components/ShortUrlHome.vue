<template>
  <div class="short-url-home">
    <div class="create-section">
      <h2>创建短链接</h2>
      <form @submit.prevent="submitUrl" class="create-form">
        <div class="form-group">
          <label for="urlInput">长链接：</label>
          <input id="urlInput" type="text" v-model="longUrl" placeholder="https://example.com/some-long-url" />
        </div>
        <div class="form-group">
          <label for="aliasInput">自定义短码：</label>
          <input id="aliasInput" type="text" v-model="alias" placeholder="可选" />
        </div>
        <div class="form-group">
          <label for="expirationInput">过期时间：</label>
          <input id="expirationInput" type="datetime-local" v-model="expirationDate" />
        </div>
        <button type="submit" :disabled="loading">生成短链接</button>
      </form>
      <div v-if="error" class="error-message">{{ error }}</div>
      <div v-if="loading" class="loading-message">处理中...</div>
      <div v-if="shortUrl" class="short-url-result">
        <p>短链接：<a :href="shortUrl" target="_blank">{{ shortUrl }}</a></p>
        <button @click="copyToClipboard">复制链接</button>
      </div>
    </div>

    <div class="list-section">
      <h2>短链接列表</h2>
      <div class="list-controls">
        <div class="search-group">
          <input type="text" v-model="searchShortCode" placeholder="短码关键字" />
          <input type="text" v-model="searchLongUrl" placeholder="长链接关键字" />
          <button @click="searchUrls">搜索</button>
        </div>
      </div>
      
      <table v-if="urlList.length > 0" class="url-table">
        <thead>
          <tr>
            <th>短码</th>
            <th>原始链接</th>
            <th>创建时间</th>
            <th>过期时间</th>
            <th>点击次数</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="url in urlList" :key="url.shortCode">
            <td><a :href="url.shortUrl" target="_blank">{{ url.shortCode }}</a></td>
            <td class="long-url">{{ url.longUrl }}</td>
            <td>{{ formatDate(url.createTime) }}</td>
            <td>{{ formatDate(url.expiresAt) }}</td>
            <td>{{ url.clickCount }}</td>
            <td :class="{ 'expired': isExpired(url.expiresAt), 'active': !isExpired(url.expiresAt) }">{{ isExpired(url.expiresAt) ? '已过期' : '有效' }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="no-data">暂无数据</div>

      <div class="pagination" v-if="total > 0">
        <button :disabled="current <= 1" @click="changePage(current - 1)">上一页</button>
        <span>第 {{ current }} 页 / 共 {{ pages }} 页 (总记录数: {{ total }})</span>
        <button :disabled="current >= pages" @click="changePage(current + 1)">下一页</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'ShortUrlHome',
  setup() {
    const longUrl = ref('')
    const shortUrl = ref('')
    const error = ref('')
    const loading = ref(false)
    const alias = ref('')
    const expirationDate = ref('')

    // 列表相关数据
    const urlList = ref([])
    const current = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const pages = ref(0)
    const searchShortCode = ref('')
    const searchLongUrl = ref('')
    const urlPattern = /^(https?:\/\/)?([a-zA-Z0-9-]+\.)+[a-zA-Z]{2,6}(\/[-a-zA-Z0-9@:%_+.~#?&//=]*)?$/;

    const validUrl = (url) => {
      const pattern = new RegExp(urlPattern);
      return !!pattern.test(url);
    }

    const submitUrl = async () => {
      error.value = '';
      shortUrl.value = '';
      if (!validUrl(longUrl.value)) {
        error.value = '请输入有效的URL';
        return;
      }
      loading.value = true;
      try {
        const response = await fetch('http://127.0.0.1:8080/api/shorturls/shorten', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            longUrl: longUrl.value,
            shortCode: alias.value || undefined,
            expiresAt: expirationDate.value || undefined
          })
        });
        const result = await response.json();
        if (result.code === 0) {
          shortUrl.value = result.data;
          // 刷新列表
          searchUrls();
        } else {
          error.value = result.message || '创建失败';
        }
      } catch (e) {
        error.value = '网络错误，请稍后重试';
      } finally {
        loading.value = false;
      }
    }

    const searchUrls = async () => {
      try {
        const response = await fetch('http://127.0.0.1:8080/api/shorturls/list', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            pageNum: current.value,
            pageSize: pageSize.value,
            shortCode: searchShortCode.value || undefined,
            longUrl: searchLongUrl.value || undefined
          })
        });
        const result = await response.json();
        if (result.code === 0) {
          urlList.value = result.data;
          total.value = result.total || 0;
          pages.value = Math.ceil(total.value / pageSize.value);
          current.value = result.current || current.value;
        } else {
          error.value = result.message || '获取列表失败';
        }
      } catch (e) {
        error.value = '网络错误，请稍后重试';
      }
    }

    const changePage = (page) => {
      current.value = page;
      searchUrls();
    }

    const copyToClipboard = async () => {
      try {
        const tempInput = document.createElement('input');
        tempInput.style.position = 'absolute';
        tempInput.style.left = '-9999px';
        tempInput.value = shortUrl.value;
        document.body.appendChild(tempInput);
        tempInput.select();
        document.execCommand('copy');
        document.body.removeChild(tempInput);
        
        error.value = '';
        const originalShortUrl = shortUrl.value;
        shortUrl.value = '复制成功！';
        setTimeout(() => {
          if (shortUrl.value === '复制成功！') {
            shortUrl.value = originalShortUrl;
          }
        }, 1500);
      } catch (err) {
        error.value = '复制失败，请手动复制';
        console.error('复制失败:', err);
      }
    }

    const getFullShortUrl = (shortCode) => {
      return `http://127.0.0.1:8080/api/shorturls/${shortCode}`;
    }

    const formatDate = (dateStr) => {
      if (!dateStr) return '-';
      return new Date(dateStr).toLocaleString('zh-CN');
    }

    const isExpired = (expiresAt) => {
      if (!expiresAt) return false;
      return new Date(expiresAt) < new Date();
    }

    onMounted(() => {
      searchUrls();
    });

    return {
      longUrl,
      shortUrl,
      error,
      loading,
      alias,
      expirationDate,
      urlList,
      current,
      pages,
      total,
      searchShortCode,
      searchLongUrl,
      submitUrl,
      copyToClipboard,
      searchUrls,
      changePage,
      getFullShortUrl,
      formatDate,
      isExpired
    };
  }
}
</script>

<style scoped>
.short-url-home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.create-section {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-group label {
  width: 100px;
  text-align: right;
}

.form-group input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  padding: 8px 16px;
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.error-message {
  color: red;
  margin-top: 10px;
}

.loading-message {
  margin-top: 10px;
  font-style: italic;
}

.short-url-result {
  margin-top: 20px;
  padding: 15px;
  background: #e8f5e9;
  border-radius: 4px;
}

.list-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.list-controls {
  margin-bottom: 20px;
}

.search-group {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.search-group input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.url-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.url-table th,
.url-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.url-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

.long-url {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 20px;
  padding: 10px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination button {
  min-width: 80px;
  background-color: #1890ff;
  transition: all 0.3s;
}

.pagination button:hover:not(:disabled) {
  background-color: #40a9ff;
}

.pagination button:disabled {
  background-color: #d9d9d9;
  color: #999;
}

.pagination span {
  font-size: 14px;
  color: #666;
  padding: 0 10px;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #666;
}

.expired {
  color: #ff4d4f;
  font-weight: bold;
}

.active {
  color: #52c41a;
  font-weight: bold;
}
</style>
