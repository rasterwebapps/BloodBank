export type SettingCategory =
  | 'GENERAL'
  | 'SECURITY'
  | 'NOTIFICATIONS'
  | 'INVENTORY'
  | 'COMPLIANCE'
  | 'INTEGRATIONS';

export interface SystemSetting {
  id: string;
  key: string;
  value: string;
  description: string;
  category: SettingCategory;
  isEditable: boolean;
  updatedAt: string;
}

export interface FeatureFlag {
  id: string;
  key: string;
  name: string;
  description: string;
  enabled: boolean;
  category: string;
  updatedAt: string;
}

export interface UpdateSettingRequest {
  value: string;
}

export interface UpdateFeatureFlagRequest {
  enabled: boolean;
}
