import React, { useState } from 'react';
import './RequestForm.css';

function RequestForm() {
  const [formData, setFormData] = useState({
    vehicleNo: '',
    vehicleType: '',
    vehicleBrand: '',
    vehicleModel: '',
    userSection: '',
    replacementDate: '',
    existingMake: '',
    tireSize: '',
    noOfTires: '',
    noOfTubes: '',
    costCenter: '',
    presentKm: '',
    previousKm: '',
    wearIndicator: 'No',
    wearPattern: 'One Edge',
    officerServiceNo: '',
    comments: '',
    images: Array(7).fill(null),
  });

  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (index, file) => {
    const updatedImages = [...formData.images];
    updatedImages[index] = file;
    setFormData(prev => ({ ...prev, images: updatedImages }));
  };

  const validate = () => {
    const newErrors = {};
    const requiredFields = [
      'vehicleNo', 'vehicleType', 'vehicleBrand', 'vehicleModel',
      'userSection', 'replacementDate', 'existingMake', 'tireSize',
      'noOfTires', 'noOfTubes', 'costCenter', 'presentKm',
      'previousKm', 'officerServiceNo'
    ];

    requiredFields.forEach(field => {
      if (!formData[field].trim()) {
        newErrors[field] = 'This field is required';
      }
    });

    if (formData.vehicleNo.length > 10) {
      newErrors.vehicleNo = 'Max 10 characters';
    }

    if (formData.costCenter.length > 15) {
      newErrors.costCenter = 'Max 15 characters';
    }

    if (formData.officerServiceNo.length > 10) {
      newErrors.officerServiceNo = 'Max 10 characters';
    }

    if (formData.comments.length > 500) {
      newErrors.comments = 'Max 500 characters';
    }

    if (formData.presentKm && (!/^\d+$/.test(formData.presentKm) || parseInt(formData.presentKm) <= 0)) {
      newErrors.presentKm = 'Must be a positive number';
    }

    if (formData.previousKm && (!/^\d+$/.test(formData.previousKm) || parseInt(formData.previousKm) < 0)) {
      newErrors.previousKm = 'Must be 0 or more';
    }

    if (
      formData.presentKm &&
      formData.previousKm &&
      parseInt(formData.presentKm) < parseInt(formData.previousKm)
    ) {
      newErrors.presentKm = 'Present KM must be ≥ Previous KM';
    }

    if (formData.noOfTires && (!/^\d+$/.test(formData.noOfTires) || parseInt(formData.noOfTires) <= 0)) {
      newErrors.noOfTires = 'Must be a number > 0';
    }

    if (formData.noOfTubes && (!/^\d+$/.test(formData.noOfTubes) || parseInt(formData.noOfTubes) < 0)) {
      newErrors.noOfTubes = 'Must be a number ≥ 0';
    }

    formData.images.forEach((img, i) => {
      if (img && !img.type.startsWith('image/')) {
        newErrors[`image${i}`] = 'Invalid image file';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      console.log('Form data:', formData);
      alert('Form submitted successfully!');
      // You can now send data to Spring Boot backend here
    }
  };

  return (
    <form className="request-form" onSubmit={handleSubmit}>
      <h2>TIRE REQUEST FORM</h2>

      {[
        { label: 'Vehicle No.', name: 'vehicleNo' },
        { label: 'Vehicle Type', name: 'vehicleType' },
        { label: 'Vehicle Brand', name: 'vehicleBrand' },
        { label: 'Vehicle Model', name: 'vehicleModel' },
        { label: 'User Section', name: 'userSection' },
        { label: 'Last Tire Replacement Date', name: 'replacementDate', type: 'date' },
        { label: 'Make of Existing Tire', name: 'existingMake' },
        { label: 'Tire Size Required', name: 'tireSize' },
        { label: 'No of Tires Required', name: 'noOfTires' },
        { label: 'No of Tubes Required', name: 'noOfTubes' },
        { label: 'Cost Center', name: 'costCenter' },
        { label: 'Present Km Reading', name: 'presentKm' },
        { label: 'Km Reading at Previous Tire Replacement', name: 'previousKm' },
        { label: 'Approving Officer Service No.', name: 'officerServiceNo' },
      ].map(field => (
        <div key={field.name} className="form-group">
          <label>{field.label}</label>
          <input
            type={field.type || 'text'}
            name={field.name}
            className={errors[field.name] ? 'error-input' : ''}
            value={formData[field.name]}
            onChange={handleChange}
          />
          {errors[field.name] && <div className="error-text">{errors[field.name]}</div>}
        </div>
      ))}

      <div className="form-group">
        <label>Tire Wear Indicator Appeared</label>
        <select name="wearIndicator" value={formData.wearIndicator} onChange={handleChange}>
          <option value="No">No</option>
          <option value="Yes">Yes</option>
        </select>
      </div>

      <div className="form-group">
        <label>Tire Wear Pattern</label>
        <select name="wearPattern" value={formData.wearPattern} onChange={handleChange}>
          <option value="One Edge">One Edge</option>
          <option value="Center">Center</option>
          <option value="Both Edges">Both Edges</option>
        </select>
      </div>

      <div className="form-group">
        <label>Comments</label>
        <textarea
          name="comments"
          className={errors.comments ? 'error-input' : ''}
          value={formData.comments}
          onChange={handleChange}
          rows={3}
        />
        {errors.comments && <div className="error-text">{errors.comments}</div>}
      </div>

      <div className="form-group">
        <label>Upload Images</label>
        {formData.images.map((_, i) => (
          <div key={i}>
            <input
              type="file"
              accept="image/*"
              onChange={(e) => handleImageChange(i, e.target.files[0])}
            />
            {errors[`image${i}`] && <div className="error-text">{errors[`image${i}`]}</div>}
          </div>
        ))}
      </div>

      <button type="submit">Send</button>
    </form>
  );
}

export default RequestForm;
