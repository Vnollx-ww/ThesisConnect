import * as XLSX from 'xlsx'

export function exportToExcel(data, sheetName, fileName) {
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
  
  const colWidths = []
  if (data.length > 0) {
    Object.keys(data[0]).forEach(key => {
      const maxLen = Math.max(
        key.length,
        ...data.map(row => String(row[key] || '').length)
      )
      colWidths.push({ wch: Math.min(maxLen + 2, 50) })
    })
  }
  worksheet['!cols'] = colWidths
  
  XLSX.writeFile(workbook, fileName)
}

export function exportToCSV(data, fileName) {
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')
  XLSX.writeFile(workbook, fileName, { bookType: 'csv' })
}
