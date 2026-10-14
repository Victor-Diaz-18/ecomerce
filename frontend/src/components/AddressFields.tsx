import { Address, Department } from '../types';

// The store only ships inside the country, so the country is not something the user types
export const SHIPPING_COUNTRY = 'Colombia';

export const STREET_TYPES = ['Calle', 'Carrera', 'Avenida', 'Avenida Calle', 'Avenida Carrera', 'Diagonal', 'Transversal', 'Circular', 'Autopista', 'Kilómetro', 'Manzana'];

// The parts of a Colombian address: "Calle 13 # 3-84, Centro, Santa Marta, Magdalena"
export interface AddressForm {
  streetType: string;
  streetNumber: string;
  crossNumber: string;
  plateNumber: string;
  neighborhood: string;
  department: string;
  city: string;
}

export const EMPTY_ADDRESS: AddressForm = {
  streetType: STREET_TYPES[0], streetNumber: '', crossNumber: '', plateNumber: '', neighborhood: '', department: '', city: '',
};

export const streetLine = (a: AddressForm) =>
  `${a.streetType} ${a.streetNumber.trim()} # ${a.crossNumber.trim()}-${a.plateNumber.trim()}`;

export const isComplete = (a: AddressForm) =>
  Boolean(a.streetNumber.trim() && a.crossNumber.trim() && a.plateNumber.trim() && a.neighborhood.trim() && a.department && a.city);

export const addressLabel = (a: Address) =>
  [a.street, a.neighborhood, a.city, a.department].filter(Boolean).join(', ');

// Puts a saved address back into the form. Old addresses may lack cross number, plate or
// neighbourhood: whatever is missing is left blank for the user to complete
export function toForm(address: Address): AddressForm {
  const full = address.street.match(/^(.+?)\s+(\w+)\s*#\s*(\w+)\s*-\s*(\w+)$/);
  const partial = address.street.match(/^(.+?)\s+(\w+)$/);
  const [streetType, streetNumber, crossNumber, plateNumber] = full
    ? full.slice(1)
    : partial ? [partial[1], partial[2], '', ''] : ['', '', '', ''];
  const knownType = STREET_TYPES.find(t => t.toLowerCase() === streetType.toLowerCase());
  return {
    streetType: knownType ?? STREET_TYPES[0],
    streetNumber, crossNumber, plateNumber,
    neighborhood: address.neighborhood ?? '',
    department: address.department ?? '',
    city: address.city,
  };
}

interface Props {
  value: AddressForm;
  onChange: (value: AddressForm) => void;
  departments: Department[] | null;
}

// The inputs of an address; the page that uses it decides what its buttons do
export function AddressFields({ value, onChange, departments }: Props) {
  const cities = departments?.find(d => d.name === value.department)?.cities ?? [];

  return (
    <>
      <div className="address-row">
        <select aria-label="Tipo de vía" value={value.streetType} onChange={e => onChange({ ...value, streetType: e.target.value })}>
          {STREET_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
        </select>
        <input aria-label="Número de la vía" type="text" placeholder="13" maxLength={10} value={value.streetNumber} onChange={e => onChange({ ...value, streetNumber: e.target.value })} />
        <span>#</span>
        <input aria-label="Número del cruce" type="text" placeholder="3" maxLength={10} value={value.crossNumber} onChange={e => onChange({ ...value, crossNumber: e.target.value })} />
        <span>-</span>
        <input aria-label="Placa" type="text" placeholder="84" maxLength={10} value={value.plateNumber} onChange={e => onChange({ ...value, plateNumber: e.target.value })} />
      </div>
      <input aria-label="Barrio" type="text" placeholder="Barrio" maxLength={100} value={value.neighborhood} onChange={e => onChange({ ...value, neighborhood: e.target.value })} />
      <div className="address-row">
        <select aria-label="Departamento" value={value.department} onChange={e => onChange({ ...value, department: e.target.value, city: '' })}>
          <option value="">Departamento</option>
          {departments?.map(d => <option key={d.name} value={d.name}>{d.name}</option>)}
        </select>
        <select aria-label="Ciudad" value={value.city} disabled={!value.department} onChange={e => onChange({ ...value, city: e.target.value })}>
          <option value="">Ciudad</option>
          {cities.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>
    </>
  );
}
