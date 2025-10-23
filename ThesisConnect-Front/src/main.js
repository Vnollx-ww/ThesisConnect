import Vue from 'vue'
import App from './App.vue'
import router from '@/router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import request from '@/utils/request'
import * as api from '@/api'

Vue.config.productionTip = false
Vue.use(ElementUI);

// 挂载axios到Vue原型
Vue.prototype.$http = request

// 挂载API方法到Vue原型
Vue.prototype.$api = api

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
