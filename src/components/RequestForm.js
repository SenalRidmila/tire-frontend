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
<<<<<<< HEAD
=======
  const [crudData, setCrudData] = useState(null); // Changed initial state
  const [isLoadingCrudData, setIsLoadingCrudData] = useState(false);
  const [crudDataError, setCrudDataError] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submissionError, setSubmissionError] = useState(null);
  const [editingItemId, setEditingItemId] = useState(null);
  const [editingItemData, setEditingItemData] = useState(null);
  const [itemActionError, setItemActionError] = useState(null);
  const [isProcessingItemAction, setIsProcessingItemAction] = useState(false);

  // Generic handler for input changes in edit mode
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditingItemData(prev => ({ ...prev, [name]: value }));
  };
>>>>>>> 3439c07 (Crud Table)

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

<<<<<<< HEAD
  const handleSubmit = (e) => {
    e.preventDefault();
    if (validate()) {
      console.log('Form data:', formData);
      alert('Form submitted successfully!');
      // You can now send data to Spring Boot backend here
    }
  };

=======
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (validate()) {
      // console.log('Form data:', formData); // Commented out as per requirement
      setIsSubmitting(true);
      setSubmissionError(null);
      // We are not fetching external CRUD data anymore before submission.
      // setIsLoadingCrudData and setCrudDataError will be used when displaying the submitted form data.
      setCrudData(null); // Clear previous submission display
      setCrudDataError(null);


      // The concept of 'combinedData' might change as we are not fetching external CRUD data first.
      // For now, we'll assume the backend only needs formData for the main submission.
      // If the backend expects something from a previous GET /api/crud-data, this needs reconsideration.
      // However, the requirement is to display formData after successful POST, so this part changes.

      const payload = JSON.stringify(formData);
      console.log(`Attempting to POST to /api/submit-request with payload: ${payload}`);

      try {
        // Simulate fetch POST request to /api/submit-request
        // const response = await fetch('/api/submit-request', {
        //   method: 'POST',
        //   headers: {
        //     'Content-Type': 'application/json',
        //   },
        //   body: payload,
        // });

        // Simulate success / failure for submission
        const mockSubmitSuccess = Math.random() > 0.0; // Simulate 100% success
        const mockResponse = { ok: mockSubmitSuccess, status: mockSubmitSuccess ? 200 : 500 };
        
        if (mockResponse.ok) {
          console.log(`POST to /api/submit-request successful. Response: ${JSON.stringify(mockResponse)}`);
          alert('Form data submitted to backend successfully!');
          
          // Now, display the submitted formData in the CRUD table area
          setIsLoadingCrudData(true); // Briefly indicate loading for the display update
          setItemActionError(null); // Clear any previous item action errors
          const submittedEntry = {
            ...formData,
            id: Date.now(), // Add a unique ID
            // Clear image fields for display as they are File objects
            images: formData.images.map(img => img ? `Image: ${img.name}` : null),
          };
          setCrudData([submittedEntry]);
          setIsLoadingCrudData(false);
          // Optionally, reset the form fields here
          // setFormData({ ...initialFormDataState }); 
          
          setIsSubmitting(false);
        } else {
          console.error("Error submitting form data to backend. Status:", mockResponse.status);
          setSubmissionError("Failed to submit form. Server responded with status " + mockResponse.status);
          alert('Error submitting form data to backend.');
          setIsSubmitting(false);
        }
      } catch (error) {
        console.error("Network error or other issue submitting data to backend:", error);
        setSubmissionError("Network error or other issue submitting data to backend: " + error.message);
        alert('Network error or other issue submitting data to backend.');
        setIsSubmitting(false);
      }
    }
  };

  const handleSaveEditing = async (itemId) => {
    setItemActionError(null);
    setIsProcessingItemAction(true);
    const payload = JSON.stringify(editingItemData);
    console.log(`Attempting to PUT to /api/request/${itemId} with payload: ${payload}`);

    try {
      // Simulate API Call
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      const mockSuccess = Math.random() > 0.1; // 90% success rate

      if (!mockSuccess) throw new Error('Simulated API save error');

      console.log(`PUT to /api/request/${itemId} successful.`);
      setCrudData(prevCrudData =>
        prevCrudData.map(dataItem =>
          dataItem.id === itemId ? { ...editingItemData, id: itemId } : dataItem
        )
      );
      setEditingItemId(null);
      setEditingItemData(null);
      // alert('Item updated successfully (simulated)!'); // Replaced by itemActionError logic
    } catch (error) {
      const errorMsg = `Failed to update item ${itemId}. ${error.message}. Please try again.`;
      console.error(`Error PUT to /api/request/${itemId}:`, errorMsg);
      setItemActionError(errorMsg);
    }
    setIsProcessingItemAction(false);
  };

  const handleCancelEditing = () => {
    console.log('Editing cancelled for item ID:', editingItemId);
    setEditingItemId(null);
    setEditingItemData(null);
    setItemActionError(null); // Clear item action error on cancel
  };

  const handleDeleteItem = async (itemId) => {
    setItemActionError(null);
    setIsProcessingItemAction(true);
    console.log(`Attempting to DELETE /api/request/${itemId}`);

    try {
      // Simulate API Call
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      const mockSuccess = Math.random() > 0.1; // 90% success rate

      if (!mockSuccess) throw new Error('Simulated API delete error');
      
      console.log(`DELETE /api/request/${itemId} successful.`);
      setCrudData(prevCrudData => prevCrudData.filter(item => item.id !== itemId));
      // alert('Item deleted successfully (simulated)!'); // Replaced

      if (editingItemId === itemId) {
        handleCancelEditing();
      }
    } catch (error) {
      const errorMsg = `Failed to delete item ${itemId}. ${error.message}. Please try again.`;
      console.error(`Error DELETE /api/request/${itemId}:`, errorMsg);
      setItemActionError(errorMsg);
    }
    setIsProcessingItemAction(false);
  };

>>>>>>> 3439c07 (Crud Table)
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

<<<<<<< HEAD
      <button type="submit">Send</button>
=======
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Submitting...' : 'Send'}
      </button>
      {submissionError && <div className="error-text submission-error">{submissionError}</div>}

      <div className="crud-data-section">
        <h3>Submitted Request Details</h3>
        {itemActionError && <div className="error-text item-action-error" style={{ color: 'red', marginBottom: '10px' }}>{itemActionError}</div>}
        {isLoadingCrudData && <p>Processing submitted data...</p>}
        {crudDataError && <div className="error-text">{crudDataError}</div>}
        {!isLoadingCrudData && !crudDataError && crudData && crudData.length > 0 && (
          <table className="crud-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Vehicle No.</th>
                <th>Vehicle Type</th>
                <th>Vehicle Brand</th>
                <th>Vehicle Model</th>
                <th>User Section</th>
                <th>Replacement Date</th>
                <th>Existing Make</th>
                <th>Tire Size</th>
                <th>No. Tires</th>
                <th>No. Tubes</th>
                <th>Cost Center</th>
                <th>Present KM</th>
                <th>Previous KM</th>
                <th>Wear Indicator</th>
                <th>Wear Pattern</th>
                <th>Officer No.</th>
                <th>Comments</th>
                <th>Image Count</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {crudData.map(item => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="vehicleNo" value={editingItemData.vehicleNo || ''} onChange={handleInputChange} />
                    ) : (
                      item.vehicleNo
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="vehicleType" value={editingItemData.vehicleType || ''} onChange={handleInputChange} />
                    ) : (
                      item.vehicleType
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="vehicleBrand" value={editingItemData.vehicleBrand || ''} onChange={handleInputChange} />
                    ) : (
                      item.vehicleBrand
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="vehicleModel" value={editingItemData.vehicleModel || ''} onChange={handleInputChange} />
                    ) : (
                      item.vehicleModel
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="userSection" value={editingItemData.userSection || ''} onChange={handleInputChange} />
                    ) : (
                      item.userSection
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="date" name="replacementDate" value={editingItemData.replacementDate || ''} onChange={handleInputChange} />
                    ) : (
                      item.replacementDate
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="existingMake" value={editingItemData.existingMake || ''} onChange={handleInputChange} />
                    ) : (
                      item.existingMake
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="tireSize" value={editingItemData.tireSize || ''} onChange={handleInputChange} />
                    ) : (
                      item.tireSize
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="number" name="noOfTires" value={editingItemData.noOfTires || ''} onChange={handleInputChange} />
                    ) : (
                      item.noOfTires
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="number" name="noOfTubes" value={editingItemData.noOfTubes || ''} onChange={handleInputChange} />
                    ) : (
                      item.noOfTubes
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="costCenter" value={editingItemData.costCenter || ''} onChange={handleInputChange} />
                    ) : (
                      item.costCenter
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="number" name="presentKm" value={editingItemData.presentKm || ''} onChange={handleInputChange} />
                    ) : (
                      item.presentKm
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="number" name="previousKm" value={editingItemData.previousKm || ''} onChange={handleInputChange} />
                    ) : (
                      item.previousKm
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <select name="wearIndicator" value={editingItemData.wearIndicator || 'No'} onChange={handleInputChange}>
                        <option value="No">No</option>
                        <option value="Yes">Yes</option>
                      </select>
                    ) : (
                      item.wearIndicator
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <select name="wearPattern" value={editingItemData.wearPattern || 'One Edge'} onChange={handleInputChange}>
                        <option value="One Edge">One Edge</option>
                        <option value="Center">Center</option>
                        <option value="Both Edges">Both Edges</option>
                      </select>
                    ) : (
                      item.wearPattern
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <input type="text" name="officerServiceNo" value={editingItemData.officerServiceNo || ''} onChange={handleInputChange} />
                    ) : (
                      item.officerServiceNo
                    )}
                  </td>
                  <td>
                    {editingItemId === item.id ? (
                      <textarea name="comments" value={editingItemData.comments || ''} onChange={handleInputChange} rows={2}></textarea>
                    ) : (
                      item.comments
                    )}
                  </td>
                  <td>{item.images && Array.isArray(item.images) ? item.images.filter(img => img !== null).length : 0}</td> {/* Not editable */}
                  <td>
                    {editingItemId === item.id ? (
                      <>
                        <button
                          onClick={() => handleSaveEditing(item.id)} 
                          style={{ marginRight: '5px' }}
                          disabled={isProcessingItemAction}
                        >
                          {isProcessingItemAction ? 'Saving...' : 'Save'}
                        </button>
                        <button onClick={handleCancelEditing} disabled={isProcessingItemAction}>Cancel</button>
                      </>
                    ) : (
                      <>
                        <button onClick={() => {
                          if (isProcessingItemAction) return; 
                          setItemActionError(null); 
                          setEditingItemId(item.id);
                          setEditingItemData({ ...item });
                          console.log('Start editing item ID:', item.id, 'with data:', item);
                        }} style={{ marginRight: '5px' }} disabled={isProcessingItemAction}>Update</button>
                        <button onClick={() => {
                          if (window.confirm('Are you sure you want to delete this item?')) {
                            handleDeleteItem(item.id);
                          }
                        }} disabled={isProcessingItemAction || editingItemId === item.id}>
                          {isProcessingItemAction && editingItemId !== item.id && !editingItemData ? 'Processing...' : 'Delete'}
                        </button>
                      </>
                    )}
                  </td>
                  {/* Render other data cells as needed */}
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!isLoadingCrudData && !crudDataError && (!crudData || crudData.length === 0) && (
          <p>No submission details to display. Submit the form to see details here.</p>
        )}
      </div>
>>>>>>> 3439c07 (Crud Table)
    </form>
  );
}

export default RequestForm;
