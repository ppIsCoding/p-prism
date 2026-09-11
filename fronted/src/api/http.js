import axios from 'axios'

export const API_BASE = import.meta.env.VITE_API_BASE || '/api'

const http = axios.create({
  baseURL: API_BASE,
  timeout: 15000,
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message =
      error?.response?.data?.message || error?.message || '网络请求失败'
    return Promise.reject(new Error(message))
  },
)

export default http
