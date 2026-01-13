import api from './api';

/**
 * Fetches users for the super admin console.
 * @param {{search?: string, role?: string}} params
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const fetchUsers = (params = {}) => {
    const requestParams = {};
    if (params.search) requestParams.search = params.search;
    if (params.role) requestParams.role = params.role;
    return api.get('/admin/users', {params: requestParams});
};

/**
 * Updates the role of a user.
 * @param {number} id
 * @param {string} role
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const updateUserRole = (id, role) => api.patch(`/admin/users/${id}/role`, {role});

/**
 * Deletes a user.
 * @param {number} id
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export const deleteUser = (id) => api.delete(`/admin/users/${id}`);