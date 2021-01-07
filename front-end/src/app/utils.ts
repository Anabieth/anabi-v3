export function enumToMap(enumObject) {
  return Object.keys(enumObject)
    .map(key => ({ value: enumObject[key], title: key }));
}
