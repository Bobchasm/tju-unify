import { createVNode, render } from 'vue'
import Toast from '@/trade/components/Toast.vue'

let toastContainer = null

const createToast = (options) => {
  if (!toastContainer) {
    toastContainer = document.createElement('div')
    toastContainer.className = 'toast-container'
    document.body.appendChild(toastContainer)
  }

  const vnode = createVNode(Toast, {
    ...options,
    onClose: () => {
      render(null, toastContainer)
    }
  })

  render(vnode, toastContainer)
}

export const toast = {
  info(message, duration = 2000) {
    createToast({ message, type: 'info', duration })
  },
  success(message, duration = 2000) {
    createToast({ message, type: 'success', duration })
  },
  error(message, duration = 2000) {
    createToast({ message, type: 'error', duration })
  },
  warning(message, duration = 2000) {
    createToast({ message, type: 'warning', duration })
  }
}
