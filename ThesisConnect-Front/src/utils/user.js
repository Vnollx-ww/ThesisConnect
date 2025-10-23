import { authApi } from '@/api'

/**
 * 获取当前用户ID
 * @returns {Promise<string|null>} 用户ID或null
 */
export async function getCurrentUserId() {
  let userId = null
  
  // 尝试从localStorage获取
  userId = localStorage.getItem('userId')
  if (userId) {
    return userId
  }
  
  // 尝试从token中获取用户信息
  const token = localStorage.getItem('token')
  if (token) {
    try {
      const response = await authApi.getUserInfo()
      if (response.code === 200 && response.data) {
        userId = response.data.id
        // 保存到localStorage
        localStorage.setItem('userId', userId)
        return userId
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果是权限问题，清除本地存储
      if (error.message && (error.message.includes('权限') || error.message.includes('登录'))) {
        clearUserInfo()
      }
    }
  }
  
  return null
}

/**
 * 获取当前用户信息
 * @returns {Promise<Object|null>} 用户信息对象或null
 */
export async function getCurrentUser() {
  const token = localStorage.getItem('token')
  if (!token) {
    return null
  }
  
  try {
    const response = await authApi.getUserInfo()
    if (response.code === 200 && response.data) {
      // 保存用户ID到localStorage
      localStorage.setItem('userId', response.data.id)
      return response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 如果是权限问题，清除本地存储
    if (error.message && (error.message.includes('权限') || error.message.includes('登录'))) {
      clearUserInfo()
    }
  }
  
  return null
}

/**
 * 检查用户是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = localStorage.getItem('token')
  return !!token
}

/**
 * 清除用户信息
 */
export function clearUserInfo() {
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
}
