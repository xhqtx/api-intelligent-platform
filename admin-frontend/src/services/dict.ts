import api from './api';
import { AxiosResponse } from 'axios';

export interface DictType {
  id: number;
  code: string;
  name: string;
  status: number;
  remark: string;
  createdAt: string;
  updatedAt: string | null;
  createdBy: string;
  updatedBy: string | null;
}

export interface DictData {
  id: number;
  dictTypeId: number;
  dictTypeCode: string;
  label: string;
  value: string;
  sort: number;
  status: number;
  defaultFlag: number;
  remark: string;
  createdAt: string;
  updatedAt: string | null;
  createdBy: string;
  updatedBy: string | null;
}

export interface DictTypeRequest {
  code: string;
  name: string;
  status: number;
  remark: string;
}

export interface DictDataRequest {
  dictTypeCode: string;
  label: string;
  value: string;
  sort: number;
  status: number;
  defaultFlag: number;
  remark: string;
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  empty: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

// 字典类型接口
export const getDictTypes = (page = 0, size = 10, sort?: string): Promise<AxiosResponse<ApiResponse<PageResponse<DictType>>>> => {
  return api.get('/auth/api/dict/types', {
    params: { page, size, sort }
  });
};

export const getDictTypeById = (id: number): Promise<AxiosResponse<ApiResponse<DictType>>> => {
  return api.get(`/auth/api/dict/types/${id}`);
};

export const createDictType = (dictType: DictTypeRequest): Promise<AxiosResponse<ApiResponse<DictType>>> => {
  return api.post('/auth/api/dict/types', dictType);
};

export const updateDictType = (id: number, dictType: DictTypeRequest): Promise<AxiosResponse<ApiResponse<DictType>>> => {
  return api.put(`/auth/api/dict/types/${id}`, dictType);
};

export const deleteDictType = (id: number): Promise<AxiosResponse<ApiResponse<null>>> => {
  return api.delete(`/auth/api/dict/types/${id}`);
};

// 字典数据接口
export const getDictDataByType = (typeCode: string): Promise<AxiosResponse<ApiResponse<DictData[]>>> => {
  return api.get(`/auth/api/dict/data/${typeCode}`);
};

export const getDictDataById = (id: number): Promise<AxiosResponse<ApiResponse<DictData>>> => {
  return api.get(`/auth/api/dict/data/item/${id}`);
};

export const createDictData = (dictData: DictDataRequest): Promise<AxiosResponse<ApiResponse<DictData>>> => {
  return api.post('/auth/api/dict/data', dictData);
};

export const updateDictData = (id: number, dictData: DictDataRequest): Promise<AxiosResponse<ApiResponse<DictData>>> => {
  return api.put(`/auth/api/dict/data/${id}`, dictData);
};

export const deleteDictData = (id: number): Promise<AxiosResponse<ApiResponse<null>>> => {
  return api.delete(`/auth/api/dict/data/${id}`);
};