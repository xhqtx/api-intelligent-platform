import { createStore } from 'vuex'
import { getCurrentUser } from '@/services/api'

interface Role {
  id: number;
  name: string;
}

interface UserState {
  id: string | null;
  username: string | null;
  email: string | null;
  roles: Role[];
  isAdmin: boolean;
  isAuthenticated: boolean;
  loading: boolean;
}

interface RootState {
  user: UserState;
}

export default createStore<RootState>({
  state: {
    user: {
      id: null,
      username: null,
      email: null,
      roles: [],
      isAdmin: false,
      isAuthenticated: false,
      loading: false
    }
  },
  getters: {
    currentUser: (state) => state.user,
    isAdmin: (state) => state.user.isAdmin,
    isAuthenticated: (state) => state.user.isAuthenticated,
    userRoles: (state) => state.user.roles
  },
  mutations: {
    SET_USER(state, user) {
      state.user.id = user.id;
      state.user.username = user.username;
      state.user.email = user.email;
      state.user.roles = user.roles || [];
      state.user.isAdmin = user.roles?.some((role: Role) => role.name === 'ADMIN') || false;
      state.user.isAuthenticated = true;
      console.log('User set in store:', state.user);
    },
    CLEAR_USER(state) {
      state.user.id = null;
      state.user.username = null;
      state.user.email = null;
      state.user.roles = [];
      state.user.isAdmin = false;
      state.user.isAuthenticated = false;
    },
    SET_LOADING(state, loading) {
      state.user.loading = loading;
    }
  },
  actions: {
    async fetchCurrentUser({ commit }) {
      commit('SET_LOADING', true);
      try {
        const response = await getCurrentUser();
        console.log('Fetched user data:', response);
        if (response && response.data) {
          commit('SET_USER', response.data);
        } else {
          throw new Error('Invalid user data received');
        }
      } catch (error) {
        console.error('获取用户信息失败', error);
        commit('CLEAR_USER');
      } finally {
        commit('SET_LOADING', false);
      }
    },
    logout({ commit }) {
      localStorage.removeItem('admin_token');
      commit('CLEAR_USER');
    }
  },
  modules: {
  }
})